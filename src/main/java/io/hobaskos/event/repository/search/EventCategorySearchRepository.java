package io.hobaskos.event.repository.search;

import io.hobaskos.event.domain.EventCategory;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the EventCategory entity.
 */
public interface EventCategorySearchRepository extends ElasticsearchRepository<EventCategory, Long> {
}
