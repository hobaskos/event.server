package io.hobaskos.event.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A EventImage.
 */
@Entity
@Table(name = "event_image")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "eventimage")
public class EventImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Size(max = 256)
    @Column(name = "title", length = 256)
    private String title;

    @NotNull
    @Size(max = 256)
    @Column(name = "image_url", length = 256, nullable = false)
    private String imageUrl;

    @ManyToOne
    @NotNull
    private EventPoll poll;

    @OneToMany(mappedBy = "eventImage")
    @JsonIgnore
    @JsonManagedReference
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<EventImageVote> votes = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public EventImage title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public EventImage imageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public EventPoll getPoll() {
        return poll;
    }

    public EventImage poll(EventPoll eventPoll) {
        this.poll = eventPoll;
        return this;
    }

    public void setPoll(EventPoll eventPoll) {
        this.poll = eventPoll;
    }

    public Set<EventImageVote> getVotes() {
        return votes;
    }

    public EventImage votes(Set<EventImageVote> eventImageVotes) {
        this.votes = eventImageVotes;
        return this;
    }

    public EventImage addVotes(EventImageVote eventImageVote) {
        votes.add(eventImageVote);
        eventImageVote.setEventImage(this);
        return this;
    }

    public EventImage removeVotes(EventImageVote eventImageVote) {
        votes.remove(eventImageVote);
        eventImageVote.setEventImage(null);
        return this;
    }

    public void setVotes(Set<EventImageVote> eventImageVotes) {
        this.votes = eventImageVotes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventImage eventImage = (EventImage) o;
        if (eventImage.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, eventImage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventImage{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", imageUrl='" + imageUrl + "'" +
            '}';
    }
}
