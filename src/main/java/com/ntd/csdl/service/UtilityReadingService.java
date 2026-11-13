package com.ntd.csdl.service;

import com.ntd.csdl.dto.UtilityReadingDTO;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.entity.UtilityReading;
import com.ntd.csdl.repo.RoomRepository;
import com.ntd.csdl.repo.UtilityReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilityReadingService {

    private final UtilityReadingRepository utilityReadingRepository;
    private final RoomRepository roomRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    // CREATE
    @org.springframework.transaction.annotation.Transactional
    public UtilityReading create(UtilityReadingDTO dto) {

        if (dto.getReadingId() == null ||
                dto.getReadingId().isBlank()) {

            throw new RuntimeException(
                    "Reading ID không được để trống"
            );
        }

        if (utilityReadingRepository.existsById(dto.getReadingId())) {
            throw new RuntimeException(
                    "Chỉ số điện nước đã tồn tại"
            );
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        )
                );
        requireUniquePeriod(dto, null);

        UtilityReading reading = new UtilityReading();

        reading.setReadingId(dto.getReadingId());
        reading.setMonth(dto.getMonth());
        reading.setYear(dto.getYear());
        reading.setElectricityPrice(dto.getElectricityPrice());
        reading.setWaterPrice(dto.getWaterPrice());
        reading.setElectricityStartIndex(
                dto.getElectricityStartIndex()
        );
        reading.setElectricityEndIndex(
                dto.getElectricityEndIndex()
        );
        reading.setWaterStartIndex(
                dto.getWaterStartIndex()
        );
        reading.setWaterEndIndex(
                dto.getWaterEndIndex()
        );

        reading.setRoom(room);

        return utilityReadingRepository.save(reading);
    }

    // READ ALL
    public List<UtilityReading> getAll() {
        return utilityReadingRepository.findAll();
    }

    // READ BY ID
    public UtilityReading getById(String id) {

        return utilityReadingRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy chỉ số"
                        )
                );
    }

    // READ BY ROOM
    public List<UtilityReading> getByRoom(String roomId) {

        return utilityReadingRepository
                .findByRoomRoomId(roomId);
    }

    // UPDATE
    @org.springframework.transaction.annotation.Transactional
    public UtilityReading update(
            String id,
            UtilityReadingDTO dto
    ) {

        UtilityReading existing = getById(id);
        requireUniquePeriod(dto,id);
        if (!jdbc.queryForList("SELECT reading_id FROM simulated_meter_periods WHERE reading_id=?", id).isEmpty()
                && (!java.util.Objects.equals(existing.getRoom().getRoomId(),dto.getRoomId())
                || !java.util.Objects.equals(existing.getYear(),dto.getYear())
                || !java.util.Objects.equals(existing.getMonth(),dto.getMonth()))) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                    "Không thể đổi phòng hoặc kỳ của chỉ số mô phỏng");
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        )
                );

        existing.setMonth(dto.getMonth());
        existing.setYear(dto.getYear());
        existing.setElectricityPrice(
                dto.getElectricityPrice()
        );
        existing.setWaterPrice(
                dto.getWaterPrice()
        );
        existing.setElectricityStartIndex(
                dto.getElectricityStartIndex()
        );
        existing.setElectricityEndIndex(
                dto.getElectricityEndIndex()
        );
        existing.setWaterStartIndex(
                dto.getWaterStartIndex()
        );
        existing.setWaterEndIndex(
                dto.getWaterEndIndex()
        );

        existing.setRoom(room);

        return utilityReadingRepository.save(existing);
    }

    private void requireUniquePeriod(UtilityReadingDTO dto,String id) {
        jdbc.queryForList("SELECT room_id FROM rooms WHERE room_id=? FOR UPDATE", dto.getRoomId());
        boolean duplicate=jdbc.queryForList("SELECT reading_id FROM utility_readings WHERE room_id=? AND year=? AND month=?",
                String.class,dto.getRoomId(),dto.getYear(),dto.getMonth()).stream().anyMatch(existing->!existing.equals(id));
        if (duplicate) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                "Phòng đã có chỉ số điện nước của tháng này");
    }

    // DELETE
    public void delete(String id) {

        if (!utilityReadingRepository.existsById(id)) {
            throw new RuntimeException(
                    "Chỉ số điện nước không tồn tại"
            );
        }

        utilityReadingRepository.deleteById(id);
    }
}
