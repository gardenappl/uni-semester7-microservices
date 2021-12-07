package ua.knu.carrental.payments.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    public static final int TYPE_REVENUE = 0;
    public static final int TYPE_REPAIR_COST = 1;
    public static final int TYPE_MAINTENANCE = 2;
    public static final int TYPE_REFUND = 3;
    public static final int TYPE_REPAIR_PAID_BY_CUSTOMER = 4;
    public static final int TYPE_PURCHASE_NEW_CAR = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uah_amount", nullable = false)
    private BigDecimal uahAmount;

    @Column(name = "request_id")
    private Integer rentRequestId;

    @Column
    private int type;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(nullable = false)
    private Instant time;
}
