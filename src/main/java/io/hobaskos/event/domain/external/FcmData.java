package io.hobaskos.event.domain.external;

public class FcmData {

    private Long eventId;

    public FcmData(Long eventId) {
        this.eventId = eventId;
    }

    public Long getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return "FcmData{" +
            "eventId=" + eventId +
            '}';
    }
}
