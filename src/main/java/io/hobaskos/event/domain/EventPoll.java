package io.hobaskos.event.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import io.hobaskos.event.domain.enumeration.EventPollStatus;

/**
 * A EventPoll.
 */
@Entity
@Table(name = "event_poll")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "eventpoll")
public class EventPoll implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(max = 256)
    @Column(name = "title", length = 256, nullable = false)
    private String title;

    @Size(max = 8192)
    @Column(name = "description", length = 8192)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventPollStatus status;

    @ManyToOne
    @NotNull
    private Event event;

    @OneToMany(mappedBy = "poll")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONE)
    private Set<EventImage> images = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public EventPoll title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public EventPoll description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventPollStatus getStatus() {
        return status;
    }

    public EventPoll status(EventPollStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(EventPollStatus status) {
        this.status = status;
    }

    public Event getEvent() {
        return event;
    }

    public EventPoll event(Event event) {
        this.event = event;
        return this;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Set<EventImage> getImages() {
        return images;
    }

    public EventPoll images(Set<EventImage> eventImages) {
        this.images = eventImages;
        return this;
    }

    public EventPoll addImages(EventImage eventImage) {
        images.add(eventImage);
        eventImage.setPoll(this);
        return this;
    }

    public EventPoll removeImages(EventImage eventImage) {
        images.remove(eventImage);
        eventImage.setPoll(null);
        return this;
    }

    public void setImages(Set<EventImage> eventImages) {
        this.images = eventImages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventPoll eventPoll = (EventPoll) o;
        if (eventPoll.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, eventPoll.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventPoll{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", description='" + description + "'" +
            ", status='" + status + "'" +
            '}';
    }
}
