package io.hobaskos.event.repository;

import io.hobaskos.event.domain.Event;

import io.hobaskos.event.domain.EventCategory;
import io.hobaskos.event.domain.Location;
import io.hobaskos.event.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository for the Event entity.
 */
@SuppressWarnings("unused")
public interface EventRepository extends JpaRepository<Event,Long> {

    @Query("select event from Event event where event.owner.login = ?#{principal.username}")
    List<Event> findByOwnerIsCurrentUser();

    Page<Event> findByOwner(User owner, Pageable pageable);

    Page<Event> findByLocationsInAndEventCategoryIn(Set<Location> locations, Set<EventCategory> eventCategories, Pageable pageable);

    Page<Event> findByEventCategory(EventCategory eventCategory, Pageable pageable);

    Optional<Event> findOneById(Long id);

    @Query("select event from Event event " +
        "left join fetch event.attendings " +
        "left join fetch event.locations " +
        "left join fetch event.polls " +
        "where event.id = ?1")
    Event findOneWithEagerRelations(Long id);

    @Query("select distinct event from Event event " +
        "left join fetch event.attendings " +
        "left join fetch event.locations " +
        "left join fetch event.polls " +
        "where event.privateEvent = 'TRUE' and event.invitationCode = ?1")
    Optional<Event> findOneWithEagerRelations(String invitationCode);

}
