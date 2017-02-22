package io.hobaskos.event.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import io.hobaskos.event.domain.enumeration.UserConnectionType;

/**
 * A UserConnection.
 */
@Entity
@Table(name = "user_connection")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "userconnection")
public class UserConnection implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private UserConnectionType type;

    @ManyToOne
    @NotNull
    private User requester;

    @ManyToOne
    @NotNull
    private User requestee;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserConnectionType getType() {
        return type;
    }

    public UserConnection type(UserConnectionType type) {
        this.type = type;
        return this;
    }

    public void setType(UserConnectionType type) {
        this.type = type;
    }

    public User getRequester() {
        return requester;
    }

    public UserConnection requester(User user) {
        this.requester = user;
        return this;
    }

    public void setRequester(User user) {
        this.requester = user;
    }

    public User getRequestee() {
        return requestee;
    }

    public UserConnection requestee(User user) {
        this.requestee = user;
        return this;
    }

    public void setRequestee(User user) {
        this.requestee = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserConnection userConnection = (UserConnection) o;
        if (userConnection.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, userConnection.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UserConnection{" +
            "id=" + id +
            ", type='" + type + "'" +
            '}';
    }
}
