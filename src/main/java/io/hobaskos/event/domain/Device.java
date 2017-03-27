package io.hobaskos.event.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import io.hobaskos.event.domain.enumeration.DeviceType;

/**
 * A Device.
 */
@Entity
@Table(name = "device")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "device")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DeviceType type;

    @NotNull
    @Size(max = 512)
    @Column(name = "token", length = 512, nullable = false)
    private String token;

    @ManyToOne
    @NotNull
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DeviceType getType() {
        return type;
    }

    public Device type(DeviceType type) {
        this.type = type;
        return this;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public Device token(String token) {
        this.token = token;
        return this;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public Device user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Device device = (Device) o;
        if (device.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, device.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Device{" +
            "id=" + id +
            ", type='" + type + "'" +
            ", token='" + token + "'" +
            '}';
    }
}
