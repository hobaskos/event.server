package io.hobaskos.event.repository;

import io.hobaskos.event.domain.EventImage;
import io.hobaskos.event.domain.EventImageVote;

import io.hobaskos.event.domain.User;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the EventImageVote entity.
 */
@SuppressWarnings("unused")
public interface EventImageVoteRepository extends JpaRepository<EventImageVote,Long> {

    @Query("select eventImageVote from EventImageVote eventImageVote where eventImageVote.user.login = ?#{principal.username}")
    List<EventImageVote> findByUserIsCurrentUser();

    Optional<EventImageVote> findFirstByEventImageAndUser(EventImage eventImage, User user);
}
