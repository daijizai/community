package asia.daijizai.community.dao.elasticsearch;

import asia.daijizai.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author daijizai
 * @version 1.0
 * @date 2022/6/17 20:36
 * @description
 */

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}
