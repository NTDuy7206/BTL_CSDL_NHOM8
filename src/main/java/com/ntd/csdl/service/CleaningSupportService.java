package com.ntd.csdl.service;

import com.ntd.csdl.entity.CleaningSupport;
import com.ntd.csdl.entity.Employee;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.repo.CleaningSupportRepository;
import com.ntd.csdl.repo.EmployeeRepository;
import com.ntd.csdl.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CleaningSupportService {

    private final CleaningSupportRepository cleaningSupportRepository;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;

    // CREATE
    public CleaningSupport create(
            String roomId,
            String employeeId,
            CleaningSupport support
    ) {

        // Kiểm tra phòng
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        ));

        // Kiểm tra nhân viên
        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy nhân viên"
                        ));

        // Gán quan hệ
        support.setRoom(room);
        support.setEmployee(employee);

        // Ngày hỗ trợ mặc định là ngày hiện tại
        if (support.getSupportDate() == null) {
            support.setSupportDate(LocalDate.now());
        }

        // Mặc định chưa hoàn thành
        if (support.getIsCompleted() == null) {
            support.setIsCompleted("NO");
        }

        return cleaningSupportRepository.save(support);
    }

    // READ ALL
    public List<CleaningSupport> getAll() {

        return cleaningSupportRepository.findAll();
    }

    // READ BY ID
    public CleaningSupport getById(String id) {

        return cleaningSupportRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy yêu cầu hỗ trợ vệ sinh"
                        ));
    }

    // Lấy công việc theo phòng
    public List<CleaningSupport> getByRoom(
            String roomId
    ) {

        return cleaningSupportRepository
                .findByRoomRoomId(roomId);
    }

    // Lấy công việc theo nhân viên
    public List<CleaningSupport> getByEmployee(
            String employeeId
    ) {

        return cleaningSupportRepository
                .findByEmployeeEmployeeId(employeeId);
    }

    // Lấy công việc chưa hoàn thành
    public List<CleaningSupport> getUncompleted() {

        return cleaningSupportRepository
                .findByIsCompleted("NO");
    }

    // UPDATE
    public CleaningSupport update(
            String id,
            CleaningSupport support
    ) {

        CleaningSupport existing = getById(id);

        existing.setSupportDate(
                support.getSupportDate()
        );

        existing.setTaskContent(
                support.getTaskContent()
        );

        existing.setIsCompleted(
                support.getIsCompleted()
        );

        return cleaningSupportRepository.save(existing);
    }

    // Đánh dấu đã hoàn thành
    public CleaningSupport complete(String id) {

        CleaningSupport support = getById(id);

        support.setIsCompleted("YES");

        return cleaningSupportRepository.save(support);
    }

    // DELETE
    public void delete(String id) {

        if (!cleaningSupportRepository.existsById(id)) {
            throw new RuntimeException(
                    "Yêu cầu hỗ trợ không tồn tại"
            );
        }

        cleaningSupportRepository.deleteById(id);
    }
}