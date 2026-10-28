package com.ntd.csdl.service;

import com.ntd.csdl.dto.RoomDTO;
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

    // =========================
    // CREATE
    // =========================

    public Room create(RoomDTO dto) {

        if (dto.getRoomId() == null ||
                dto.getRoomId().isBlank()) {

            throw new RuntimeException(
                    "Room ID không được để trống"
            );
        }

        if (dto.getBasePrice() != null &&
                dto.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Giá phòng không được âm"
            );
        }

        if (roomRepository.existsById(dto.getRoomId())) {

            throw new RuntimeException(
                    "Phòng đã tồn tại"
            );
        }

        Room room = new Room();

        room.setRoomId(dto.getRoomId());
        room.setFloor(dto.getFloor());
        room.setArea(dto.getArea());
        room.setRoomType(dto.getRoomType());
        room.setStatus(dto.getStatus());
        room.setBasePrice(dto.getBasePrice());

        return roomRepository.save(room);
    }

    // =========================
    // READ
    // =========================

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

    // =========================
    // UPDATE
    // =========================

    public Room update(String id, RoomDTO dto) {

        Room existing = getById(id);

        if (dto.getBasePrice() != null &&
                dto.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Giá phòng không được âm"
            );
        }

        existing.setFloor(dto.getFloor());
        existing.setArea(dto.getArea());
        existing.setRoomType(dto.getRoomType());
        existing.setStatus(dto.getStatus());
        existing.setBasePrice(dto.getBasePrice());

        return roomRepository.save(existing);
    }

    // =========================
    // DELETE
    // =========================

    public void delete(String id) {

        if (!roomRepository.existsById(id)) {

            throw new RuntimeException(
                    "Phòng không tồn tại"
            );
        }

        roomRepository.deleteById(id);
    }

    // =========================
    // SEARCH
    // =========================

    public List<Room> getByStatus(String status) {
        return roomRepository.findByStatus(status);
    }

    public List<Room> getByType(String type) {
        return roomRepository.findByRoomType(type);
    }

    public List<Room> findByMaxPrice(BigDecimal price) {
        return roomRepository
                .findByBasePriceLessThanEqual(price);
    }
}