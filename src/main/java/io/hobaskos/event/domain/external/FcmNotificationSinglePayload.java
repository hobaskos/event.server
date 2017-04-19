package io.hobaskos.event.domain.external;

/**
 * Created by alex on 3/3/17.
 */
public class FcmNotificationSinglePayload {

    private String to;

    private FcmNotification notification;

    private FcmData data;

    public FcmNotificationSinglePayload(String to, FcmNotification notification, FcmData data) {
        this.to = to;
        this.notification = notification;
        this.data = data;
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

    public FcmData getData() {
        return data;
    }

    public void setData(FcmData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FcmNotificationSinglePayload{" +
            "to='" + to + '\'' +
            ", notification=" + notification +
            ", data=" + data +
            '}';
    }
}
