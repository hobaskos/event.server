package io.hobaskos.event.repository.search;

import io.hobaskos.event.domain.EventPoll;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the EventPoll entity.
 */
public interface EventPollSearchRepository extends ElasticsearchRepository<EventPoll, Long> {
}
