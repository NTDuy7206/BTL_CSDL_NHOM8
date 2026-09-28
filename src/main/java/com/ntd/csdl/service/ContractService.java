package com.ntd.csdl.service;

import com.ntd.csdl.entity.Contract;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.entity.Tenant;
import com.ntd.csdl.repo.ContractRepository;
import com.ntd.csdl.repo.RoomRepository;
import com.ntd.csdl.repo.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;

    // CREATE CONTRACT
    public Contract create(
            String roomId,
            String tenantId,
            Contract contract
    ) {

        // Kiểm tra phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng"
                        ));

        // Kiểm tra người thuê
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy người thuê"
                        ));

        // Kiểm tra phòng
        if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            throw new RuntimeException(
                    "Phòng hiện không còn trống"
            );
        }

        // Kiểm tra ngày
        if (contract.getStartDate() != null &&
                contract.getEndDate() != null &&
                contract.getEndDate()
                        .isBefore(contract.getStartDate())) {

            throw new RuntimeException(
                    "Ngày kết thúc phải sau ngày bắt đầu"
            );
        }

        // Kiểm tra tiền cọc
        if (contract.getDeposit() != null &&
                contract.getDeposit()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Tiền cọc không được âm"
            );
        }

        contract.setRoom(room);
        contract.setTenant(tenant);

        Contract saved = contractRepository.save(contract);

        // Sau khi tạo hợp đồng
        // phòng chuyển sang OCCUPIED
        room.setStatus("OCCUPIED");
        roomRepository.save(room);

        return saved;
    }

    // READ
    public List<Contract> getAll() {
        return contractRepository.findAll();
    }

    public Contract getById(String id) {
        return contractRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hợp đồng"
                        ));
    }

    // DELETE
    public void delete(String id) {

        Contract contract = getById(id);

        Room room = contract.getRoom();

        if (room != null) {
            room.setStatus("AVAILABLE");
            roomRepository.save(room);
        }

        contractRepository.deleteById(id);
    }

    // Hợp đồng hết hạn
    public List<Contract> getExpiredContracts() {

        return contractRepository
                .findExpiredContracts(LocalDate.now());
    }

    // Hợp đồng của người thuê
    public List<Contract> getByTenant(String tenantId) {

        return contractRepository
                .findByTenantTenantId(tenantId);
    }

    // Hợp đồng của phòng
    public List<Contract> getByRoom(String roomId) {

        return contractRepository
                .findByRoomRoomId(roomId);
    }
}