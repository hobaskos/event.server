package io.hobaskos.event.repository;

import io.hobaskos.event.domain.Location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Location entity.
 */
@SuppressWarnings("unused")
public interface LocationRepository extends JpaRepository<Location,Long> {

    Page<Location> findByEventId(Long eventId, Pageable pageable);
}
