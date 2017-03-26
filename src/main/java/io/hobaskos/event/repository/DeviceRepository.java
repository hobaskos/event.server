package io.hobaskos.event.repository;

import io.hobaskos.event.domain.Device;

import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.enumeration.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository for the Device entity.
 */
@SuppressWarnings("unused")
public interface DeviceRepository extends JpaRepository<Device,Long> {

    @Query("select device from Device device where device.user.login = ?#{principal.username}")
    List<Device> findByUserIsCurrentUser();

    @Query("select device from Device device where device.user.login = ?#{principal.username}")
    Page<Device> findByUserIsCurrentUser(Pageable pageable);

    Set<Device> findByUserIn(Set<User> users);

    Device findFirstByUserAndTokenAndType(User user, String token, DeviceType type);
}
