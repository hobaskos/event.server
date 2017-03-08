package io.hobaskos.event.repository.search;

import io.hobaskos.event.domain.EventImage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the EventImage entity.
 */
public interface EventImageSearchRepository extends ElasticsearchRepository<EventImage, Long> {
}
