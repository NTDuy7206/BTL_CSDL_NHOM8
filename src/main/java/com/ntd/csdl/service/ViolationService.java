package com.ntd.csdl.service;

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

    public Violation create(
            String roomId,
            String ruleId,
            Violation violation
    ) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        ));

        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy quy định"
                        ));

        violation.setRoom(room);
        violation.setRule(rule);

        if (violation.getViolationDate() == null) {
            violation.setViolationDate(LocalDate.now());
        }

        // Nếu chưa nhập tiền phạt thực tế
        // sử dụng mức phạt của Rule
        if (violation.getActualFine() == null) {

            violation.setActualFine(
                    rule.getPrescribedFine()
            );
        }

        if (violation.getActualFine()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Tiền phạt không được âm"
            );
        }

        return violationRepository.save(violation);
    }

    public List<Violation> getAll() {
        return violationRepository.findAll();
    }

    public Violation getById(String id) {

        return violationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy vi phạm"
                        ));
    }

    public List<Violation> getByRoom(String roomId) {

        return violationRepository
                .findByRoomRoomId(roomId);
    }

    public void delete(String id) {

        if (!violationRepository.existsById(id)) {
            throw new RuntimeException(
                    "Vi phạm không tồn tại"
            );
        }

        violationRepository.deleteById(id);
    }
}