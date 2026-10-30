package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Transient
    private Long landlordId;

    @Transient
    private String landlordName;

    @Transient
    private Boolean occupied;
    @Transient
    private Boolean occupancyKnown;
    @Transient
    private Boolean availableForRent;

    @Id
    @Column(name = "room_id", length = 20)
    private String roomId;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "area")
    private Float area;

    @Column(name = "room_type", length = 50)
    private String roomType;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "base_price", precision = 18, scale = 2)
    private BigDecimal basePrice;
}