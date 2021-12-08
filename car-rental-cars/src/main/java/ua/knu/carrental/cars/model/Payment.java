package ua.knu.carrental.cars.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public class Payment {
    public static final int TYPE_REVENUE = 0;
    public static final int TYPE_REPAIR_COST = 1;
    public static final int TYPE_MAINTENANCE = 2;
    public static final int TYPE_REFUND = 3;
    public static final int TYPE_REPAIR_PAID_BY_CUSTOMER = 4;
    public static final int TYPE_PURCHASE_NEW_CAR = 5;

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private BigDecimal uahAmount;

    @Getter
    @Setter
    private Integer rentRequestId;

    @Getter
    @Setter
    private int type;

    @Getter
    @Setter
    private Car car;

    @Getter
    @Setter
    private String time;
}
