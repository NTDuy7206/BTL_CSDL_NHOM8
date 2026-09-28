package com.ntd.csdl.service;

import com.ntd.csdl.entity.Incident;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.repo.IncidentRepository;
import com.ntd.csdl.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final RoomRepository roomRepository;

    public Incident create(
            String roomId,
            Incident incident
    ) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        ));

        incident.setRoom(room);

        if (incident.getReportDate() == null) {
            incident.setReportDate(LocalDate.now());
        }

        if (incident.getStatus() == null) {
            incident.setStatus("PENDING");
        }

        return incidentRepository.save(incident);
    }

    public List<Incident> getAll() {
        return incidentRepository.findAll();
    }

    public Incident getById(String id) {

        return incidentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy sự cố"
                        ));
    }

    public List<Incident> getByRoom(String roomId) {

        return incidentRepository
                .findByRoomRoomId(roomId);
    }

    public List<Incident> getByStatus(String status) {

        return incidentRepository
                .findByStatus(status);
    }

    public void delete(String id) {

        if (!incidentRepository.existsById(id)) {
            throw new RuntimeException(
                    "Sự cố không tồn tại"
            );
        }

        incidentRepository.deleteById(id);
    }
}