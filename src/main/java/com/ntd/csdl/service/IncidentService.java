package com.ntd.csdl.service;

import com.ntd.csdl.dto.IncidentDTO;
import com.ntd.csdl.entity.Incident;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.repo.IncidentRepository;
import com.ntd.csdl.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final RoomRepository roomRepository;

    // =========================
    // CREATE
    // =========================
    public Incident create(IncidentDTO dto) {

        if (dto.getIncidentId() == null ||
                dto.getIncidentId().isBlank()) {

            throw new RuntimeException(
                    "Incident ID không được để trống"
            );
        }

        if (incidentRepository.existsById(
                dto.getIncidentId())) {

            throw new RuntimeException(
                    "Sự cố đã tồn tại"
            );
        }

        Room room = roomRepository.findById(
                dto.getRoomId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Không tìm thấy phòng"
                )
        );

        Incident incident = new Incident();

        incident.setIncidentId(
                dto.getIncidentId()
        );

        incident.setIncidentContent(
                dto.getIncidentContent()
        );

        incident.setReportDate(
                dto.getReportDate()
        );

        incident.setStatus(
                dto.getStatus()
        );

        incident.setRoom(room);

        return incidentRepository.save(incident);
    }

    // =========================
    // GET ALL
    // =========================
    public List<Incident> getAll() {

        return incidentRepository.findAll();
    }

    // =========================
    // GET BY ID
    // =========================
    public Incident getById(String id) {

        return incidentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy sự cố"
                        )
                );
    }

    // =========================
    // GET BY ROOM
    // =========================
    public List<Incident> getByRoom(
            String roomId) {

        return incidentRepository
                .findByRoomRoomId(roomId);
    }

    // =========================
    // GET BY STATUS
    // =========================
    public List<Incident> getByStatus(
            String status) {

        return incidentRepository
                .findByStatus(status);
    }

    // =========================
    // UPDATE
    // =========================
    public Incident update(
            String id,
            IncidentDTO dto) {

        Incident existing =
                incidentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy sự cố"
                                )
                        );

        Room room =
                roomRepository.findById(
                        dto.getRoomId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        )
                );

        existing.setIncidentContent(
                dto.getIncidentContent()
        );

        existing.setReportDate(
                dto.getReportDate()
        );

        existing.setStatus(
                dto.getStatus()
        );

        existing.setRoom(room);

        return incidentRepository.save(existing);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(String id) {

        if (!incidentRepository.existsById(id)) {

            throw new RuntimeException(
                    "Sự cố không tồn tại"
            );
        }

        incidentRepository.deleteById(id);
    }
}