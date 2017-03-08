package io.hobaskos.event.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import io.hobaskos.event.domain.enumeration.EventPollStatus;

/**
 * A DTO for the EventPoll entity.
 */
public class EventPollDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 256)
    private String title;

    @Size(max = 8192)
    private String description;

    @NotNull
    private EventPollStatus status;


    private Long eventId;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public EventPollStatus getStatus() {
        return status;
    }

    public void setStatus(EventPollStatus status) {
        this.status = status;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventPollDTO eventPollDTO = (EventPollDTO) o;

        if ( ! Objects.equals(id, eventPollDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventPollDTO{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", description='" + description + "'" +
            ", status='" + status + "'" +
            '}';
    }
}
