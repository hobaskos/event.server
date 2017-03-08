package io.hobaskos.event.service.dto;

import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import io.hobaskos.event.domain.enumeration.EventAttendingType;

/**
 * A DTO for the EventUserAttending entity.
 */
public class EventUserAttendingDTO implements Serializable {

    private Long id;

    private ZonedDateTime createdDate;

    @NotNull
    private EventAttendingType type;

    private Long eventId;

    private String userLogin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }
    public EventAttendingType getType() {
        return type;
    }

    public void setType(EventAttendingType type) {
        this.type = type;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventUserAttendingDTO eventUserAttendingDTO = (EventUserAttendingDTO) o;

        if ( ! Objects.equals(id, eventUserAttendingDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventUserAttendingDTO{" +
            "id=" + id +
            ", createdDate='" + createdDate + "'" +
            ", type='" + type + "'" +
            '}';
    }
}
