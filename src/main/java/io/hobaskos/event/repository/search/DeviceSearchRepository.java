package io.hobaskos.event.repository.search;

import io.hobaskos.event.domain.Device;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Device entity.
 */
public interface DeviceSearchRepository extends ElasticsearchRepository<Device, Long> {
}
