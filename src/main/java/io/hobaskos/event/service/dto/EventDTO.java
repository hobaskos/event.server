package io.hobaskos.event.service.dto;

import io.hobaskos.event.domain.enumeration.EventAttendingType;

import javax.persistence.Lob;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;


/**
 * A DTO for the Event entity.
 */
public class EventDTO implements Serializable {

    private Long id;

    @Size(max = 256)
    private String title;

    @Size(max = 8192)
    private String description;

    @Size(max = 512)
    private String imageUrl;

    private String imageContentType;

    @Size(max = 5000000)
    @Lob
    private byte[] image;

    private ZonedDateTime fromDate;

    private ZonedDateTime toDate;

    @NotNull
    private Boolean privateEvent;

    @Size(max = 64)
    private String invitationCode;

    private String ownerLogin;

    private String ownerFirstName;

    private String ownerLastName;

    private Set<LocationDTO> locations;

    private EventCategoryDTO eventCategory;

    private EventUserAttendingDTO myAttendance;

    private int attendanceCount;

    private Long defaultPollId;

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
    public ZonedDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(ZonedDateTime fromDate) {
        this.fromDate = fromDate;
    }
    public ZonedDateTime getToDate() {
        return toDate;
    }

    public void setToDate(ZonedDateTime toDate) {
        this.toDate = toDate;
    }

    public Boolean getPrivateEvent() {
        return privateEvent;
    }

    public void setPrivateEvent(Boolean privateEvent) {
        this.privateEvent = privateEvent;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public Set<LocationDTO> getLocations() {
        return locations;
    }

    public void setLocations(Set<LocationDTO> locations) {
        this.locations = locations;
    }

    public EventCategoryDTO getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategoryDTO eventCategory) {
        this.eventCategory = eventCategory;
    }

    public EventUserAttendingDTO getMyAttendance() {
        return myAttendance;
    }

    public void setMyAttendance(EventUserAttendingDTO myAttendance) {
        this.myAttendance = myAttendance;
    }

    public int getAttendanceCount() {
        return attendanceCount;
    }

    public void setAttendanceCount(int attendanceCount) {
        this.attendanceCount = attendanceCount;
    }

    public Long getDefaultPollId() {
        return defaultPollId;
    }

    public void setDefaultPollId(Long defaultPollId) {
        this.defaultPollId = defaultPollId;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public void setOwnerFirstName(String ownerFirstName) {
        this.ownerFirstName = ownerFirstName;
    }

    public String getOwnerLastName() {
        return ownerLastName;
    }

    public void setOwnerLastName(String ownerLastName) {
        this.ownerLastName = ownerLastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventDTO eventDTO = (EventDTO) o;

        if ( ! Objects.equals(id, eventDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventDTO{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", description='" + description + "'" +
            ", imageUrl='" + imageUrl + "'" +
            ", fromDate='" + fromDate + "'" +
            ", toDate='" + toDate + "'" +
            ", privateEvent='" + privateEvent + "'" +
            ", invitationCode='" + invitationCode + "'" +
            '}';
    }
}
