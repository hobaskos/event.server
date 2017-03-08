package io.hobaskos.event.repository.search;

import io.hobaskos.event.domain.EventUserAttending;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the EventUserAttending entity.
 */
public interface EventUserAttendingSearchRepository extends ElasticsearchRepository<EventUserAttending, Long> {
}
