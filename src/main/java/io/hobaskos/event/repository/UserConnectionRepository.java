package io.hobaskos.event.repository;

import io.hobaskos.event.domain.UserConnection;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the UserConnection entity.
 */
@SuppressWarnings("unused")
public interface UserConnectionRepository extends JpaRepository<UserConnection,Long> {

    @Query("select userConnection from UserConnection userConnection where userConnection.requester.login = ?#{principal.username}")
    List<UserConnection> findByRequesterIsCurrentUser();

    @Query("select userConnection from UserConnection userConnection where userConnection.requestee.login = ?#{principal.username}")
    List<UserConnection> findByRequesteeIsCurrentUser();

}
