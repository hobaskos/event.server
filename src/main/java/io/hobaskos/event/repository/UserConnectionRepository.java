package io.hobaskos.event.repository;

import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.UserConnection;

import io.hobaskos.event.domain.enumeration.UserConnectionType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the UserConnection entity.
 */
@SuppressWarnings("unused")
public interface UserConnectionRepository extends JpaRepository<UserConnection,Long> {

    @Query("select userConnection from UserConnection userConnection where userConnection.requester.login = ?#{principal.username}")
    List<UserConnection> findByRequesterIsCurrentUser();

    @Query("select userConnection from UserConnection userConnection where userConnection.requestee.login = ?#{principal.username}")
    List<UserConnection> findByRequesteeIsCurrentUser();

    @Query("select uc from UserConnection uc left join uc.requester left join uc.requestee" +
        " where (uc.requester=:requester or uc.requestee=:requestee) and uc.type = :connectionType")
    List<UserConnection> findByRequesterOrRequesteeAndType(@Param("requester")User requester,
                                                           @Param("requestee") User requestee,
                                                           @Param("connectionType") UserConnectionType connectionType);

    Optional<UserConnection> findOneByRequesterAndRequesteeAndType(User requester, User requestee, UserConnectionType type);

    List<UserConnection> findByRequesteeAndType(User requestee, UserConnectionType type);

    List<UserConnection> findByRequesterAndType(User requester, UserConnectionType type);
}
