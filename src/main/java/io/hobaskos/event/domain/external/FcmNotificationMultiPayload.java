package io.hobaskos.event.domain.external;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by alex on 3/3/17.
 */
public class FcmNotificationMultiPayload {

    @SerializedName("registration_ids")
    private List<String> recipients;

    private FcmNotification notification;

    public FcmNotificationMultiPayload(List<String> recipients, FcmNotification notification) {
        this.recipients = recipients;
        this.notification = notification;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public FcmNotification getNotification() {
        return notification;
    }

    public void setNotification(FcmNotification notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return "FcmNotificationMultiPayload{" +
            "recipients=" + recipients +
            ", notification=" + notification +
            '}';
    }
}
