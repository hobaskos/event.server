package io.hobaskos.event.repository;

import io.hobaskos.event.domain.Device;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Device entity.
 */
@SuppressWarnings("unused")
public interface DeviceRepository extends JpaRepository<Device,Long> {

    @Query("select device from Device device where device.user.login = ?#{principal.username}")
    List<Device> findByUserIsCurrentUser();

    @Query("select device from Device device where device.user.login = ?#{principal.username}")
    Page<Device> findByUserIsCurrentUser(Pageable pageable);
}
