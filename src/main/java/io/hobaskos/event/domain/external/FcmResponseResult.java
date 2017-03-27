package io.hobaskos.event.domain.external;

import com.google.gson.annotations.SerializedName;

public class FcmResponseResult {

    @SerializedName("message_id")
    private String messageId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "FcmResponseResult{" +
            "messageId='" + messageId + '\'' +
            '}';
    }
}
