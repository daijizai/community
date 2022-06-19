package asia.daijizai.community.event;

import asia.daijizai.community.controller.UserController;
import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.entity.Event;
import asia.daijizai.community.entity.Message;
import asia.daijizai.community.service.DiscussPostService;
import asia.daijizai.community.service.ElasticsearchService;
import asia.daijizai.community.service.MessageService;
import asia.daijizai.community.util.CommunityConstant;
import asia.daijizai.community.util.SensitiveFilter;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    //消费点赞、关注、评论事件
    @KafkaListener(topics = {TOPIC_LIKE, TOPIC_FOLLOW, TOPIC_COMMENT})
    public void handleNoticeMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);//通过record拿到事件
        if (event == null) {
            logger.error("消息的格式有误！");
            return;
        }

        // 发送站内通知
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

    //消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);//通过record拿到事件
        if (event == null) {
            logger.error("消息的格式有误！");
            return;
        }

        DiscussPost discussPost = discussPostService.getDiscussPost(event.getEntityId());
        sensitiveFilter.filter(discussPost);//过滤敏感词

        elasticsearchService.saveDiscussPost(discussPost);
    }
}
