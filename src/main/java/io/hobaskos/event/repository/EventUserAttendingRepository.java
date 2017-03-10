package io.hobaskos.event.repository;

import io.hobaskos.event.domain.Event;
import io.hobaskos.event.domain.EventUserAttending;

import io.hobaskos.event.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the EventUserAttending entity.
 */
@SuppressWarnings("unused")
public interface EventUserAttendingRepository extends JpaRepository<EventUserAttending,Long> {

    @Query("select eventUserAttending from EventUserAttending eventUserAttending where eventUserAttending.user.login = ?#{principal.username}")
    List<EventUserAttending> findByUserIsCurrentUser();

    Page<EventUserAttending> findByEvent(Event event, Pageable pageable);

    Page<EventUserAttending> findByUser(User user, Pageable pageable);

    Optional<EventUserAttending> findOneByEventAndUser(Event event, User user);
}
