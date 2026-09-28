package com.ntd.csdl.service;

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

    public UtilityReading create(
            String roomId,
            UtilityReading reading
    ) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        ));

        reading.setRoom(room);

        return utilityReadingRepository.save(reading);
    }

    public List<UtilityReading> getAll() {
        return utilityReadingRepository.findAll();
    }

    public UtilityReading getById(String id) {

        return utilityReadingRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy chỉ số"
                        ));
    }

    public List<UtilityReading> getByRoom(
            String roomId
    ) {

        return utilityReadingRepository
                .findByRoomRoomId(roomId);
    }

    public void delete(String id) {

        if (!utilityReadingRepository.existsById(id)) {
            throw new RuntimeException(
                    "Chỉ số điện nước không tồn tại"
            );
        }

        utilityReadingRepository.deleteById(id);
    }
}