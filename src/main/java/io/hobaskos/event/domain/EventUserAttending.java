package io.hobaskos.event.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

import io.hobaskos.event.domain.enumeration.EventAttendingType;

/**
 * A EventUserAttending.
 */
@Entity
@Table(name = "event_user_attending")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "eventuserattending")
public class EventUserAttending implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "created_date", nullable = false)
    private ZonedDateTime createdDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EventAttendingType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @JsonBackReference
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public EventUserAttending createdDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public EventAttendingType getType() {
        return type;
    }

    public EventUserAttending type(EventAttendingType type) {
        this.type = type;
        return this;
    }

    public void setType(EventAttendingType type) {
        this.type = type;
    }

    public Event getEvent() {
        return event;
    }

    public EventUserAttending event(Event event) {
        this.event = event;
        return this;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public EventUserAttending user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventUserAttending eventUserAttending = (EventUserAttending) o;
        if (eventUserAttending.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, eventUserAttending.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventUserAttending{" +
            "id=" + id +
            ", createdDate='" + createdDate + "'" +
            ", type='" + type + "'" +
            '}';
    }
}
