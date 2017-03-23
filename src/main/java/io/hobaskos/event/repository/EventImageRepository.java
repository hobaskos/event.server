package io.hobaskos.event.repository;

import io.hobaskos.event.domain.EventImage;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the EventImage entity.
 */
@SuppressWarnings("unused")
public interface EventImageRepository extends JpaRepository<EventImage,Long> {

    @Query("select eventImage from EventImage eventImage where eventImage.user.login = ?#{principal.username}")
    List<EventImage> findByUserIsCurrentUser();

}
