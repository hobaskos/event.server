package io.hobaskos.event.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import io.hobaskos.event.domain.enumeration.UserConnectionType;

/**
 * A DTO for the UserConnection entity.
 */
public class UserConnectionDTO implements Serializable {

    private Long id;

    @NotNull
    private UserConnectionType type;


    private Long requesterId;

    private Long requesteeId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public UserConnectionType getType() {
        return type;
    }

    public void setType(UserConnectionType type) {
        this.type = type;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long userId) {
        this.requesterId = userId;
    }

    public Long getRequesteeId() {
        return requesteeId;
    }

    public void setRequesteeId(Long userId) {
        this.requesteeId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserConnectionDTO userConnectionDTO = (UserConnectionDTO) o;

        if ( ! Objects.equals(id, userConnectionDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UserConnectionDTO{" +
            "id=" + id +
            ", type='" + type + "'" +
            '}';
    }
}
