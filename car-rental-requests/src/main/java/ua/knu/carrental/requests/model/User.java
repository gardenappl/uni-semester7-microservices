package ua.knu.carrental.requests.model;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "passport_id")
    private long passportId;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "keycloak_id", unique = true, nullable = false)
    private String keycloakId;
}
