package io.hobaskos.event.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Lob;


/**
 * A DTO for the EventImage entity.
 */
public class EventImageDTO implements Serializable {

    private Long id;

    @Size(max = 256)
    private String title;

    private String imageUrl;

    @NotNull
    @Lob
    private byte[] file;

    private String fileContentType;

    private Long pollId;

    private String userLogin;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public Long getPollId() {
        return pollId;
    }

    public void setPollId(Long eventPollId) {
        this.pollId = eventPollId;
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

        EventImageDTO eventImageDTO = (EventImageDTO) o;

        if ( ! Objects.equals(id, eventImageDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EventImageDTO{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", file='" + file + "'" +
            '}';
    }
}
