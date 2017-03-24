package io.hobaskos.event.domain.external;

/**
 * Created by alex on 3/3/17.
 */
public class FcmNotificationSinglePayload {

    private String to;

    private FcmNotification notification;

    public FcmNotificationSinglePayload(String to, FcmNotification notification) {
        this.to = to;
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public FcmNotification getNotification() {
        return notification;
    }

    public void setNotification(FcmNotification notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return "FcmNotificationSinglePayload{" +
            "to='" + to + '\'' +
            ", notification=" + notification +
            '}';
    }
}
