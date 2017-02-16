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

import io.hobaskos.event.domain.enumeration.EventCategoryTheme;

/**
 * A EventCategory.
 */
@Entity
@Table(name = "event_category")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "eventcategory")
public class EventCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(max = 32)
    @Column(name = "title", length = 32, nullable = false)
    private String title;

    @NotNull
    @Size(max = 256)
    @Column(name = "icon_url", length = 256, nullable = false)
    private String iconUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false)
    private EventCategoryTheme theme;

    @OneToMany(mappedBy = "eventCategory")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Event> events = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public EventCategory title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public EventCategory iconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public EventCategoryTheme getTheme() {
        return theme;
    }

    public EventCategory theme(EventCategoryTheme theme) {
        this.theme = theme;
        return this;
    }

    public void setTheme(EventCategoryTheme theme) {
        this.theme = theme;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public EventCategory events(Set<Event> events) {
        this.events = events;
        return this;
    }

    public EventCategory addEvents(Event event) {
        events.add(event);
        event.setEventCategory(this);
        return this;
    }

    public EventCategory removeEvents(Event event) {
        events.remove(event);
        event.setEventCategory(null);
        return this;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventCategory eventCategory = (EventCategory) o;
        if (eventCategory.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, eventCategory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventCategory{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", iconUrl='" + iconUrl + "'" +
            ", theme='" + theme + "'" +
            '}';
    }
}
