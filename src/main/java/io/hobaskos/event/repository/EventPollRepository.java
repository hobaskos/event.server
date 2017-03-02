package io.hobaskos.event.repository;

import io.hobaskos.event.domain.EventPoll;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the EventPoll entity.
 */
@SuppressWarnings("unused")
public interface EventPollRepository extends JpaRepository<EventPoll,Long> {

}
