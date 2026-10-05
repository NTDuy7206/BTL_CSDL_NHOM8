package com.ntd.csdl.service;

import com.ntd.csdl.dto.CleaningSupportDTO;
import com.ntd.csdl.entity.CleaningSupport;
import com.ntd.csdl.entity.Employee;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.repo.CleaningSupportRepository;
import com.ntd.csdl.repo.EmployeeRepository;
import com.ntd.csdl.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CleaningSupportService {

    private final CleaningSupportRepository cleaningSupportRepository;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;

    // =========================
    // CREATE
    // =========================
    public CleaningSupport create(CleaningSupportDTO dto) {

        if (dto.getSupportId() == null) {
            throw new RuntimeException(
                    "Support ID không được để trống"
            );
        }

        if (cleaningSupportRepository.existsById(dto.getSupportId())) {
            throw new RuntimeException(
                    "Bản ghi hỗ trợ đã tồn tại"
            );
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        )
                );

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy nhân viên"
                        )
                );

        CleaningSupport support = new CleaningSupport();

        support.setSupportId(dto.getSupportId());
        support.setSupportDate(dto.getSupportDate());
        support.setTaskContent(dto.getTaskContent());
        support.setIsCompleted(dto.getIsCompleted());

        support.setRoom(room);
        support.setEmployee(employee);

        return cleaningSupportRepository.save(support);
    }

    // =========================
    // GET ALL
    // =========================
    public List<CleaningSupport> getAll() {

        return cleaningSupportRepository.findAll();
    }

    // =========================
    // GET BY ID
    // =========================
    public CleaningSupport getById(String id) {

        return cleaningSupportRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy yêu cầu hỗ trợ vệ sinh"
                        )
                );
    }

    // =========================
    // GET BY ROOM
    // =========================
    public List<CleaningSupport> getByRoom(String roomId) {

        return cleaningSupportRepository
                .findByRoomRoomId(roomId);
    }

    // =========================
    // GET BY EMPLOYEE
    // =========================
    public List<CleaningSupport> getByEmployee(
            String employeeId) {

        return cleaningSupportRepository
                .findByEmployeeEmployeeId(employeeId);
    }

    // =========================
    // GET UNCOMPLETED
    // =========================
    public List<CleaningSupport> getUncompleted() {

        return cleaningSupportRepository
                .findByIsCompleted(false);
    }

    // =========================
    // UPDATE
    // =========================
    public CleaningSupport update(
            String id,
            CleaningSupportDTO dto) {

        CleaningSupport existing = getById(id);

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        )
                );

        Employee employee = employeeRepository.findById(
                dto.getEmployeeId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Không tìm thấy nhân viên"
                )
        );

        existing.setSupportDate(
                dto.getSupportDate()
        );

        existing.setTaskContent(
                dto.getTaskContent()
        );

        existing.setIsCompleted(
                dto.getIsCompleted()
        );

        existing.setRoom(room);
        existing.setEmployee(employee);

        return cleaningSupportRepository.save(existing);
    }

    // =========================
    // COMPLETE
    // =========================
    public CleaningSupport complete(String id) {

        CleaningSupport support = getById(id);

        support.setIsCompleted(true);

        return cleaningSupportRepository.save(support);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(String id) {

        if (!cleaningSupportRepository.existsById(id)) {
            throw new RuntimeException(
                    "Yêu cầu hỗ trợ không tồn tại"
            );
        }

        cleaningSupportRepository.deleteById(id);
    }
}