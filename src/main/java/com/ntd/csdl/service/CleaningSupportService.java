package com.ntd.csdl.service;

import com.ntd.csdl.dto.CleaningSupportDTO;
import com.ntd.csdl.entity.CleaningSupport;
import com.ntd.csdl.entity.Employee;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.repo.CleaningSupportRepository;
import com.ntd.csdl.repo.EmployeeRepository;
import com.ntd.csdl.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CleaningSupportService {

    private final CleaningSupportRepository cleaningSupportRepository;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;
    private final RentalWorkflowService rentals;
    private final LandlordService landlords;
    private final JdbcTemplate jdbc;

    public record TenantRequest(String supportId, LocalDate supportDate, String taskContent,
                                boolean isCompleted, String roomId) {}

    @Transactional
    public TenantRequest requestCleaning(String username, String roomId, String content) {
        if (roomId == null || roomId.isBlank() || content == null || content.isBlank() || content.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chọn phòng và nhập nội dung từ 1 đến 500 ký tự");
        }
        rentals.requireRoom(username, roomId);
        if (landlords.owner(roomId) == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phòng chưa có chủ nhà để nhận yêu cầu");
        }
        String id = RentalWorkflowService.id("HT");
        LocalDate today = StayService.today();
        String task = content.trim();
        jdbc.update("INSERT INTO cleaning_supports(support_id,support_date,task_content,is_completed,room_id) VALUES (?,?,?,?,?)",
                id, today, task, false, roomId);
        jdbc.update("INSERT INTO cleaning_support_requesters(support_id,user_id) VALUES (?,?)",
                id, landlords.accountId(username));
        return new TenantRequest(id, today, task, false, roomId);
    }

    public List<TenantRequest> mine(String username) {
        return jdbc.query("""
                SELECT c.support_id, c.support_date, c.task_content, c.is_completed, c.room_id
                FROM cleaning_supports c JOIN cleaning_support_requesters r ON r.support_id=c.support_id
                WHERE r.user_id=? ORDER BY c.support_date DESC, c.support_id
                """, (row, index) -> new TenantRequest(row.getString("support_id"),
                row.getObject("support_date", LocalDate.class), row.getString("task_content"),
                row.getBoolean("is_completed"), row.getString("room_id")), landlords.accountId(username));
    }

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
        if (!java.util.Objects.equals(existing.getRoom().getRoomId(), dto.getRoomId()) &&
                !jdbc.queryForList("SELECT support_id FROM cleaning_support_requesters WHERE support_id=?", id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu của khách thuê phải giữ nguyên phòng đã chọn");
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        )
                );

        Employee employee = dto.getEmployeeId() == null || dto.getEmployeeId().isBlank() ? null : employeeRepository.findById(
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
