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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViolationService {

    private final ViolationRepository violationRepository;
    private final RoomRepository roomRepository;
    private final RuleRepository ruleRepository;

    // CREATE
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
                        : LocalDate.now()
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

        return violationRepository.save(violation);
    }

    // READ ALL
    public List<Violation> getAll() {
        return violationRepository.findAll();
    }

    // READ BY ID
    public Violation getById(String id) {

        return violationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy vi phạm"
                        )
                );
    }

    // READ BY ROOM
    public List<Violation> getByRoom(String roomId) {

        return violationRepository
                .findByRoomRoomId(roomId);
    }

    // UPDATE
    public Violation update(
            String id,
            ViolationDTO dto
    ) {

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

        return violationRepository.save(existing);
    }

    // DELETE
    public void delete(String id) {

        if (!violationRepository.existsById(id)) {
            throw new RuntimeException(
                    "Vi phạm không tồn tại"
            );
        }

        violationRepository.deleteById(id);
    }
}