package io.hobaskos.event.service;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.domain.external.FcmData;
import io.hobaskos.event.domain.external.FcmNotification;
import io.hobaskos.event.repository.DeviceRepository;
import io.hobaskos.event.repository.EventUserAttendingRepository;
import io.hobaskos.event.service.external.FcmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TriggerService {

    private final Logger log = LoggerFactory.getLogger(TriggerService.class);

    @Inject
    private EventUserAttendingRepository eventUserAttendingRepository;

    @Inject
    private DeviceRepository deviceRepository;

    @Inject
    private FcmService fcmService;

    @Async
    public void eventImageUploaded(EventImage eventImage, Event event) {
        Set<User> users = eventUserAttendingRepository.findByEvent(event)
            .stream().map(EventUserAttending::getUser).collect(Collectors.toSet());
        log.info("Image uploaded, sending notification to users: {}",
            users.stream().map(User::getLogin).collect(Collectors.toList()));

        Set<String> deviceTokens = deviceRepository.findByUserIn(users)
            .stream().map(Device::getToken).collect(Collectors.toSet());
        log.info("Found {} devices", deviceTokens.toArray());

        FcmNotification notification = new FcmNotification();
        notification.setTitle(String.format("Event: %s", event.getTitle()));
        notification.setBody(String.format("%s added a new image", eventImage.getUser().getFirstName()));

        fcmService.sendNotificationsToDevices(deviceTokens, notification, new FcmData(event.getId()))
            .subscribe(fcmResponse -> {
                    log.info("FcmResponse Success!");
                },
                throwable -> {
                    log.error(throwable.getMessage());
                });
    }
}
