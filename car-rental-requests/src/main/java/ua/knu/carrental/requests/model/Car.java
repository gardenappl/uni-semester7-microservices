package ua.knu.carrental.requests.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String manufacturer;

    @Column(name = "uah_per_day", nullable = false)
    private BigDecimal uahPerDay;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "current_user_id")
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String description;

    @Column(name = "uah_purchase", nullable = false)
    private BigDecimal uahPurchase;
}
