package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "utility_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityReading {

    @Id
    @Column(name = "reading_id", length = 20)
    private String readingId;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    @Column(name = "electricity_price", precision = 18, scale = 2)
    private BigDecimal electricityPrice;

    @Column(name = "water_price", precision = 18, scale = 2)
    private BigDecimal waterPrice;

    @Column(name = "electricity_start_index")
    private Integer electricityStartIndex;

    @Column(name = "electricity_end_index")
    private Integer electricityEndIndex;

    @Column(name = "water_start_index")
    private Integer waterStartIndex;

    @Column(name = "water_end_index")
    private Integer waterEndIndex;

    // FK -> rooms.room_id
    @ManyToOne
    @JoinColumn(
            name = "room_id",
            referencedColumnName = "room_id"
    )
    private Room room;
}