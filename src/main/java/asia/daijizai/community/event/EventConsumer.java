package asia.daijizai.community.event;

import asia.daijizai.community.controller.UserController;
import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.entity.Event;
import asia.daijizai.community.entity.Message;
import asia.daijizai.community.service.DiscussPostService;
import asia.daijizai.community.service.ElasticsearchService;
import asia.daijizai.community.service.MessageService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.CommunityUtil;
import asia.daijizai.community.util.SensitiveFilter;
import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/14 10:36
 * @description
 */

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.name}")
    private String bucketName;


    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    //????????????????????????????????????
    @KafkaListener(topics = {TOPIC_LIKE, TOPIC_FOLLOW, TOPIC_COMMENT})
    public void handleNoticeMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("????????????????????????");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);//??????record????????????
        if (event == null) {
            logger.error("????????????????????????");
            return;
        }

        // ??????????????????
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));

        messageService.insertMessage(message);
    }

    //??????????????????
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("????????????????????????");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);//??????record????????????
        if (event == null) {
            logger.error("????????????????????????");
            return;
        }

        DiscussPost discussPost = discussPostService.getDiscussPost(event.getEntityId());
        sensitiveFilter.filter(discussPost);//???????????????

        elasticsearchService.saveDiscussPost(discussPost);
    }

    //??????????????????
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("????????????????????????");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);//??????record????????????
        if (event == null) {
            logger.error("????????????????????????");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    // ??????????????????
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("?????????????????????!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("??????????????????!");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("??????????????????: " + cmd);
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
        }

        // ???????????????,???????????????,???????????????,?????????????????????.
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);

    }

    class UploadTask implements Runnable {

        // ????????????
        private String fileName;
        // ????????????
        private String suffix;
        // ??????????????????????????????????????????????????????
        private Future future;
        // ????????????
        private long startTime;
        // ????????????
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // ????????????
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("??????????????????,????????????:" + fileName);
                future.cancel(true);
                return;
            }
            // ????????????
            if (uploadTimes >= 3) {
                logger.error("??????????????????,????????????:" + fileName);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                logger.info(String.format("?????????%d?????????[%s].", ++uploadTimes, fileName));
                // ??????????????????
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // ??????????????????
                Auth auth = Auth.create(accessKey, secretKey);
                String key ="community-share/"+fileName;
                String uploadToken = auth.uploadToken(bucketName, key, 3600, policy);
                // ??????????????????
                UploadManager manager = new UploadManager(new Configuration(Region.region2()));
                try {
                    // ??????????????????
                    Response response = manager.put(
                            path, key, uploadToken, null, "image/" + suffix, false);
                    // ??????????????????
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("???%d???????????????[%s].%s", uploadTimes, fileName,e.getMessage()));
                }
            } else {
                logger.info("??????????????????[" + fileName + "].");
            }
        }
    }

}
