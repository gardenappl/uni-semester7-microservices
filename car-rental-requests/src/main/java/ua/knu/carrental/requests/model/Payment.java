package ua.knu.carrental.requests.model;

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

    @Setter
    @Getter
    private int id;

    @Setter
    @Getter
    private BigDecimal uahAmount;

    @Setter
    @Getter
    private Integer rentRequestId;

    @Setter
    @Getter
    private int type;

    @Setter
    @Getter
    private Car car;

    @Setter
    @Getter
    private String time;
}
