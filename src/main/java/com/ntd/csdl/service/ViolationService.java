package com.ntd.csdl.service;

import com.ntd.csdl.dto.ViolationDTO;
import com.ntd.csdl.entity.Rule;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.entity.Violation;
import com.ntd.csdl.repo.RuleRepository;
import com.ntd.csdl.repo.RoomRepository;
import com.ntd.csdl.repo.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViolationService {

    private final ViolationRepository violationRepository;
    private final RoomRepository roomRepository;
    private final RuleRepository ruleRepository;
    private final JdbcTemplate jdbc;

    // CREATE
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Violation create(ViolationDTO dto) {

        if (dto.getViolationId() == null ||
                dto.getViolationId().isBlank()) {

            throw new RuntimeException(
                    "Violation ID không được để trống"
            );
        }

        if (violationRepository.existsById(
                dto.getViolationId())) {

            throw new RuntimeException(
                    "Vi phạm đã tồn tại"
            );
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        )
                );

        Rule rule = ruleRepository.findById(dto.getRuleId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy quy định"
                        )
                );

        Violation violation = new Violation();

        violation.setViolationId(dto.getViolationId());
        violation.setViolationName(dto.getViolationName());
        violation.setViolationDate(
                dto.getViolationDate() != null
                        ? dto.getViolationDate()
                        : StayService.today()
        );
        violation.setDetailedDescription(
                dto.getDetailedDescription()
        );

        violation.setRoom(room);
        violation.setRule(rule);

        // Nếu không nhập tiền phạt thực tế
        // lấy mức phạt từ Rule
        if (dto.getActualFine() == null) {

            violation.setActualFine(
                    rule.getPrescribedFine()
            );

        } else {

            violation.setActualFine(
                    dto.getActualFine()
            );
        }

        if (violation.getActualFine() != null &&
                violation.getActualFine()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Tiền phạt không được âm"
            );
        }

        Violation saved = violationRepository.saveAndFlush(violation);
        InvoiceFines.issue(jdbc,saved,false);
        return saved;
    }

    // READ ALL
    public List<Violation> getAll() {
        return violationRepository.findAll();
    }

    // READ BY ID
    public Violation getById(String id) {

        return violationRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi"));
    }

    // READ BY ROOM
    public List<Violation> getByRoom(String roomId) {

        return violationRepository
                .findByRoomRoomId(roomId);
    }

    // UPDATE
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Violation update(
            String id,
            ViolationDTO dto
    ) {

        requireUnbilled(id);
        Violation existing = getById(id);

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        )
                );

        Rule rule = ruleRepository.findById(dto.getRuleId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy quy định"
                        )
                );

        existing.setViolationName(
                dto.getViolationName()
        );

        existing.setViolationDate(
                dto.getViolationDate()
        );

        existing.setDetailedDescription(
                dto.getDetailedDescription()
        );

        existing.setRoom(room);
        existing.setRule(rule);

        if (dto.getActualFine() == null) {

            existing.setActualFine(
                    rule.getPrescribedFine()
            );

        } else {

            if (dto.getActualFine()
                    .compareTo(BigDecimal.ZERO) < 0) {

                throw new RuntimeException(
                        "Tiền phạt không được âm"
                );
            }

            existing.setActualFine(
                    dto.getActualFine()
            );
        }

        Violation saved = violationRepository.saveAndFlush(existing);
        InvoiceFines.issue(jdbc,saved,true);
        return saved;
    }

    // DELETE
    @Transactional
    public void delete(String id) {
        requireUnbilled(id);

        if (!violationRepository.existsById(id)) {
            throw new RuntimeException(
                    "Vi phạm không tồn tại"
            );
        }

        violationRepository.deleteById(id);
    }

    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public String issueInvoice(String id) {
        jdbc.queryForList("SELECT violation_id FROM violations WHERE violation_id=? FOR UPDATE",id);
        var invoices = jdbc.queryForList("SELECT invoice_id FROM invoice_violation_charges WHERE violation_id=?",String.class,id);
        if (!invoices.isEmpty()) return invoices.getFirst();
        var violation = getById(id);
        if (violation.getActualFine()==null || violation.getActualFine().signum()<=0)
            throw new IllegalArgumentException("Vi phạm chưa có tiền phạt để lập hóa đơn");
        InvoiceFines.issue(jdbc,violation,true);
        return jdbc.queryForObject("SELECT invoice_id FROM invoice_violation_charges WHERE violation_id=?",String.class,id);
    }

    private void requireUnbilled(String id) {
        jdbc.queryForList("SELECT violation_id FROM violations WHERE violation_id=? FOR UPDATE",id);
        if (InvoiceFines.linked(jdbc,id))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Vi phạm đã được ghi vào hóa đơn");
    }
}
