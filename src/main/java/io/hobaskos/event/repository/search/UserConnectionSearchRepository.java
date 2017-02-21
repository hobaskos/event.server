package io.hobaskos.event.repository.search;

import io.hobaskos.event.domain.UserConnection;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the UserConnection entity.
 */
public interface UserConnectionSearchRepository extends ElasticsearchRepository<UserConnection, Long> {
}
