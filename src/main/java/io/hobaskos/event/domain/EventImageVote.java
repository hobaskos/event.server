package io.hobaskos.event.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A EventImageVote.
 */
@Entity
@Table(name = "event_image_vote")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "eventimagevote")
public class EventImageVote implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Min(value = -1)
    @Max(value = 1)
    @Column(name = "vote", nullable = false)
    private Integer vote;

    @ManyToOne
    @NotNull
    private User user;

    @ManyToOne
    @NotNull
    @JsonBackReference
    private EventImage eventImage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVote() {
        return vote;
    }

    public EventImageVote vote(Integer vote) {
        this.vote = vote;
        return this;
    }

    public void setVote(Integer vote) {
        this.vote = vote;
    }

    public User getUser() {
        return user;
    }

    public EventImageVote user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EventImage getEventImage() {
        return eventImage;
    }

    public EventImageVote eventImage(EventImage eventImage) {
        this.eventImage = eventImage;
        return this;
    }

    public void setEventImage(EventImage eventImage) {
        this.eventImage = eventImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventImageVote eventImageVote = (EventImageVote) o;
        if (eventImageVote.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, eventImageVote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventImageVote{" +
            "id=" + id +
            ", vote='" + vote + "'" +
            '}';
    }
}
