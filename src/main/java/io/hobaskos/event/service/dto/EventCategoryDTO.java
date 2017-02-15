package io.hobaskos.event.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import javax.persistence.Lob;

import io.hobaskos.event.domain.enumeration.EventCategoryTheme;

/**
 * A DTO for the EventCategory entity.
 */
public class EventCategoryDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 32)
    private String title;

    @Size(max = 256)
    private String iconUrl;

    @Lob
    @Size(max = 5000000)
    private byte[] icon;

    private String iconContentType;

    @NotNull
    private EventCategoryTheme theme;

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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public String getIconContentType() {
        return iconContentType;
    }

    public void setIconContentType(String iconContentType) {
        this.iconContentType = iconContentType;
    }
    public EventCategoryTheme getTheme() {
        return theme;
    }

    public void setTheme(EventCategoryTheme theme) {
        this.theme = theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventCategoryDTO eventCategoryDTO = (EventCategoryDTO) o;

        if ( ! Objects.equals(id, eventCategoryDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventCategoryDTO{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", theme='" + theme + "'" +
            '}';
    }
}
