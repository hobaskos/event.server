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

    private Integer myVote;

    @Min(value = 0)
    private Integer voteCount;

    private Integer voteScore;

    @NotNull
    @Lob
    private byte[] file;

    private String fileContentType;

    private Long pollId;

    private String userLogin;

    private String userFirstName;

    private String userLastName;

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

    public Integer getMyVote() {
        return myVote;
    }

    public void setMyVote(Integer myVote) {
        this.myVote = myVote;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public Integer getVoteScore() {
        return voteScore;
    }

    public void setVoteScore(Integer voteScore) {
        this.voteScore = voteScore;
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

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
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
            ", imageUrl='" + imageUrl + "'" +
            ", voteCount='" + voteCount + "'" +
            ", voteScore='" + voteScore + "'" +
            '}';
    }
}
