package io.hobaskos.event.repository;

import io.hobaskos.event.domain.EventUserAttending;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the EventUserAttending entity.
 */
@SuppressWarnings("unused")
public interface EventUserAttendingRepository extends JpaRepository<EventUserAttending,Long> {

    @Query("select eventUserAttending from EventUserAttending eventUserAttending where eventUserAttending.user.login = ?#{principal.username}")
    List<EventUserAttending> findByUserIsCurrentUser();

}
