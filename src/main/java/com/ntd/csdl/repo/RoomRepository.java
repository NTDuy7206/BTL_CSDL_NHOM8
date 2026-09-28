package com.ntd.csdl.repo;

import com.ntd.csdl.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

    // Derived Query
    List<Room> findByStatus(String status);

    List<Room> findByRoomType(String roomType);

    List<Room> findByFloor(Integer floor);

    List<Room> findByBasePriceLessThanEqual(BigDecimal price);

    List<Room> findByAreaGreaterThan(Float area);

    // JPQL
    @Query("""
        SELECT r
        FROM Room r
        WHERE r.status = :status
    """)
    List<Room> findRoomsByStatus(String status);

    // Thống kê số phòng theo trạng thái
    @Query("""
        SELECT r.status, COUNT(r)
        FROM Room r
        GROUP BY r.status
    """)
    List<Object[]> countRoomsByStatus();

    // Thống kê số phòng theo loại
    @Query("""
        SELECT r.roomType, COUNT(r)
        FROM Room r
        GROUP BY r.roomType
    """)
    List<Object[]> countRoomsByType();
}