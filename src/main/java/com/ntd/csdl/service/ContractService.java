package com.ntd.csdl.service;

import com.ntd.csdl.dto.ContractDTO;
import com.ntd.csdl.entity.Contract;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.entity.Tenant;
import com.ntd.csdl.repo.ContractRepository;
import com.ntd.csdl.repo.RoomRepository;
import com.ntd.csdl.repo.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    // CREATE CONTRACT
    public Contract create(ContractDTO dto) {

        if (contractRepository.existsById(dto.getContractId())) {
            throw new RuntimeException("Hợp đồng đã tồn tại");
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy phòng"));

        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy tenant"));

        Contract contract = new Contract();

        contract.setContractId(dto.getContractId());
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setDeposit(dto.getDeposit());

        contract.setRoom(room);
        contract.setTenant(tenant);

        return contractRepository.save(contract);
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


    //UpDate


    @org.springframework.transaction.annotation.Transactional
    public Contract update(String id, ContractDTO dto) {
        jdbc.queryForList("SELECT contract_id FROM contracts WHERE contract_id=? FOR UPDATE", id);

        Contract existing = contractRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy hợp đồng"));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy phòng"));

        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy tenant"));

        if (VipRentalService.balanceForContract(jdbc, id) != null &&
                (!java.util.Objects.equals(existing.getStartDate(), dto.getStartDate()) ||
                 !java.util.Objects.equals(existing.getEndDate(), dto.getEndDate()) || dto.getDeposit() == null ||
                 existing.getDeposit().compareTo(dto.getDeposit()) != 0)) {
            throw new IllegalArgumentException("Ngày thuê và tiền cọc của hợp đồng VIP đã được chốt");
        }
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setDeposit(dto.getDeposit());

        existing.setRoom(room);
        existing.setTenant(tenant);

        return contractRepository.save(existing);
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
