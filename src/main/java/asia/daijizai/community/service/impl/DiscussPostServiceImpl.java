package asia.daijizai.community.service.impl;

import asia.daijizai.community.dao.DiscussPostDao;
import asia.daijizai.community.entity.DiscussPost;
import asia.daijizai.community.service.DiscussPostService;
import asia.daijizai.community.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/5 23:09
 * @description
 */

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Autowired
    private DiscussPostDao discussPostDao;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;//最多能存多少

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;//数据过期时间

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(key -> {
                    if (key == null || key.length() == 0) {
                        throw new IllegalArgumentException("参数错误!");
                    }

                    String[] params = key.split(":");
                    if (params == null || params.length != 2) {
                        throw new IllegalArgumentException("参数错误!");
                    }

                    int offset = Integer.parseInt(params[0]);
                    int limit = Integer.valueOf(params[1]);

                    // 二级缓存: Redis -> mysql

                    logger.info("load post list from DB.");
                    return discussPostDao.listDiscussPost(0, offset, limit, 1);
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(key -> {
                    logger.info("load post rows from DB.");
                    return discussPostDao.countDiscussPost(key);
                });
    }

    public List<DiscussPost> listDiscussPost(int userId, int offset, int limit, int orderMode) {
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }

        logger.info("load post list from DB.");
        return discussPostDao.listDiscussPost(userId, offset, limit, orderMode);
    }

    public int countDiscussPost(int userId) {
        if (userId == 0) {
            return postRowsCache.get(userId);
        }

        logger.info("load post rows from DB.");
        return discussPostDao.countDiscussPost(userId);
    }

    public int insertDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        return discussPostDao.insert(discussPost);
    }

    public DiscussPost getDiscussPost(int id) {
        return discussPostDao.getDiscussPost(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostDao.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostDao.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostDao.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostDao.updateScore(id, score);
    }

}
