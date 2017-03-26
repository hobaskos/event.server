package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.service.dto.DeviceDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Device and its DTO DeviceDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, })
public interface DeviceMapper {

    @Mapping(source = "user.login", target = "userLogin")
    DeviceDTO deviceToDeviceDTO(Device device);

    List<DeviceDTO> devicesToDeviceDTOs(List<Device> devices);

    @Mapping(target = "user", ignore = true)
    Device deviceDTOToDevice(DeviceDTO deviceDTO);

    List<Device> deviceDTOsToDevices(List<DeviceDTO> deviceDTOs);
}
