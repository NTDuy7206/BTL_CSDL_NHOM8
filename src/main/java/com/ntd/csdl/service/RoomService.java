package com.ntd.csdl.service;

import com.ntd.csdl.entity.Room;
import com.ntd.csdl.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    // CREATE
    public Room create(Room room) {

        if (room.getRoomId() == null ||
                room.getRoomId().isBlank()) {
            throw new RuntimeException("Room ID không được để trống");
        }

        if (room.getBasePrice() != null &&
                room.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Giá phòng không được âm"
            );
        }

        if (roomRepository.existsById(room.getRoomId())) {
            throw new RuntimeException("Phòng đã tồn tại");
        }

        return roomRepository.save(room);
    }

    // READ
    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    public Room getById(String id) {
        return roomRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        ));
    }

    // UPDATE
    public Room update(String id, Room room) {

        Room existing = getById(id);

        if (room.getBasePrice() != null &&
                room.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Giá phòng không được âm"
            );
        }

        existing.setFloor(room.getFloor());
        existing.setArea(room.getArea());
        existing.setRoomType(room.getRoomType());
        existing.setStatus(room.getStatus());
        existing.setBasePrice(room.getBasePrice());

        return roomRepository.save(existing);
    }

    // DELETE
    public void delete(String id) {

        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Phòng không tồn tại");
        }

        roomRepository.deleteById(id);
    }

    // Tìm phòng theo trạng thái
    public List<Room> getByStatus(String status) {
        return roomRepository.findByStatus(status);
    }

    // Tìm phòng theo loại
    public List<Room> getByType(String type) {
        return roomRepository.findByRoomType(type);
    }

    // Tìm phòng có giá <= mức giá
    public List<Room> findByMaxPrice(BigDecimal price) {
        return roomRepository.findByBasePriceLessThanEqual(price);
    }
}