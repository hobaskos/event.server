package io.hobaskos.event.repository.search;

import io.hobaskos.event.domain.EventImageVote;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the EventImageVote entity.
 */
public interface EventImageVoteSearchRepository extends ElasticsearchRepository<EventImageVote, Long> {
}
