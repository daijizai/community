package asia.daijizai.community.service;

import asia.daijizai.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/19 15:42
 * @description
 */

public interface ElasticsearchService {

    void saveDiscussPost(DiscussPost discussPost);

    void deleteDiscussPost(int id);

    Page<DiscussPost> searchDiscussPost(String keyWord, int current, int limit);


}
