package io.hobaskos.event.repository;

import io.hobaskos.event.domain.Event;

import io.hobaskos.event.domain.EventCategory;
import io.hobaskos.event.domain.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for the Event entity.
 */
@SuppressWarnings("unused")
public interface EventRepository extends JpaRepository<Event,Long> {

    @Query("select event from Event event where event.owner.login = ?#{principal.username}")
    List<Event> findByOwnerIsCurrentUser();

    Page<Event> findByLocationsIn(Set<Location> locations, Pageable pageable);

    Page<Event> findByEventCategory(EventCategory eventCategory, Pageable pageable);
}
