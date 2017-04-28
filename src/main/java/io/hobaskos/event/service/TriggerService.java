package io.hobaskos.event.service;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.domain.enumeration.EventAttendingType;
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
        sendNotification(createEventNotification(event, String.format("%s added a new image", eventImage.getUser().getName())), event);
    }

    @Async
    public void eventChanged(Event event) {
        sendNotification(createEventNotification(event, String.format("Changes has been made to the event")), event);
    }

    @Async
    public void eventNewAttendingUser(Event event, User user) {
        sendNotification(createEventNotification(event, String.format("%s is attending the event", user.getName())), event);
    }

    public void eventRemovedAttendingUser(Event event, User user) {
        sendNotification(createEventNotification(event, String.format("%s is no longer attending the event", user.getName())), event);
    }

    @Async
    public void eventLocationAdded(Event event, Location location) {
        sendNotification(createEventNotification(event, String.format("A new location %s has been added", location.getName())), event);
    }

    @Async
    public void eventLocationDeleted(Event event, String locationName) {
        sendNotification(createEventNotification(event, String.format("Location %s has been deleted", locationName)), event);
    }

    @Async
    public void eventLocationChanged(Event event, Location location) {
        sendNotification(createEventNotification(event, String.format("Location %s has been changed", location.getName())), event);
    }

    private FcmNotification createEventNotification(Event event, String body) {
        return new FcmNotification(String.format("Event: %s", event.getTitle()), body);
    }

    private Set<String> getAttendingDevicesForEvent(Event event) {
        Set<User> users = eventUserAttendingRepository.findByEvent(event)
            .stream().map(EventUserAttending::getUser).collect(Collectors.toSet());
        log.info("Image uploaded, sending notification to users: {}",
            users.stream().map(User::getLogin).collect(Collectors.toList()));

        Set<String> deviceTokens = deviceRepository.findByUserIn(users)
            .stream().map(Device::getToken).collect(Collectors.toSet());
        log.info("Found {} devices", deviceTokens.toArray());

        return deviceTokens;
    }

    public void sendNotification(FcmNotification fcmNotification, Event event) {
        fcmService.sendNotificationsToDevices(getAttendingDevicesForEvent(event), fcmNotification, new FcmData(event.getId()))
            .subscribe(fcmResponse -> log.info("FcmResponse Success!"),
                throwable -> log.error(throwable.getMessage()));
    }
}
