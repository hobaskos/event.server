package io.hobaskos.event.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;


/**
 * A DTO for the EventImageVote entity.
 */
public class EventImageVoteDTO implements Serializable {

    private Long id;

    @NotNull
    @Min(value = -1)
    @Max(value = 1)
    private Integer vote;

    private String userLogin;

    private Long eventImageId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getVote() {
        return vote;
    }

    public void setVote(Integer vote) {
        this.vote = vote;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Long getEventImageId() {
        return eventImageId;
    }

    public void setEventImageId(Long eventImageId) {
        this.eventImageId = eventImageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventImageVoteDTO eventImageVoteDTO = (EventImageVoteDTO) o;

        if ( ! Objects.equals(id, eventImageVoteDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventImageVoteDTO{" +
            "id=" + id +
            ", vote='" + vote + "'" +
            '}';
    }
}
