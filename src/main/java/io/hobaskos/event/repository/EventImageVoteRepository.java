package io.hobaskos.event.repository;

import io.hobaskos.event.domain.EventImageVote;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the EventImageVote entity.
 */
@SuppressWarnings("unused")
public interface EventImageVoteRepository extends JpaRepository<EventImageVote,Long> {

    @Query("select eventImageVote from EventImageVote eventImageVote where eventImageVote.user.login = ?#{principal.username}")
    List<EventImageVote> findByUserIsCurrentUser();

}
