package com.ntd.csdl.repo;

import com.ntd.csdl.entity.UtilityReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UtilityReadingRepository
        extends JpaRepository<UtilityReading, String> {

    // Derived Query
    List<UtilityReading> findByRoomRoomId(String roomId);

    List<UtilityReading> findByMonthAndYear(
            Integer month,
            Integer year
    );

    // JPQL
    @Query("""
        SELECT u
        FROM UtilityReading u
        WHERE u.room.roomId = :roomId
        AND u.month = :month
        AND u.year = :year
    """)
    UtilityReading findByRoomAndMonthAndYear(
            String roomId,
            Integer month,
            Integer year
    );

    // Thống kê tiền điện theo phòng
    @Query("""
        SELECT u.room.roomId,
               SUM(u.electricityPrice)
        FROM UtilityReading u
        GROUP BY u.room.roomId
    """)
    List<Object[]> totalElectricityPriceByRoom();
}