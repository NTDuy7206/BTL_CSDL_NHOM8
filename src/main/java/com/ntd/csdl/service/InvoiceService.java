package com.ntd.csdl.service;

import com.ntd.csdl.dto.InvoiceDTO;
import com.ntd.csdl.entity.Contract;
import com.ntd.csdl.entity.Invoice;
import com.ntd.csdl.repo.ContractRepository;
import com.ntd.csdl.repo.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    // CREATE
    @org.springframework.transaction.annotation.Transactional
    public Invoice create(InvoiceDTO dto) {
        jdbc.queryForList("SELECT contract_id FROM contracts WHERE contract_id=? FOR UPDATE",dto.getContractId());
        if(!jdbc.queryForList("SELECT invoice_id FROM invoices WHERE contract_id=? AND month=? AND year=?",dto.getContractId(),dto.getMonth(),dto.getYear()).isEmpty())throw new IllegalArgumentException("Hợp đồng đã có hóa đơn trong tháng này");

        if (dto.getInvoiceId() == null ||
                dto.getInvoiceId().isBlank()) {

            throw new RuntimeException(
                    "Invoice ID không được để trống"
            );
        }

        if (invoiceRepository.existsById(
                dto.getInvoiceId())) {

            throw new RuntimeException(
                    "Hóa đơn đã tồn tại"
            );
        }

        Contract contract = contractRepository
                .findById(dto.getContractId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hợp đồng"
                        )
                );

        Invoice invoice = new Invoice();

        invoice.setInvoiceId(dto.getInvoiceId());
        invoice.setMonth(dto.getMonth());
        invoice.setYear(dto.getYear());
        invoice.setRentAmount(dto.getRentAmount());
        invoice.setElectricityAmount(
                dto.getElectricityAmount()
        );
        invoice.setWaterAmount(
                dto.getWaterAmount()
        );
        invoice.setFineAmount(
                dto.getFineAmount()
        );

        invoice.setContract(contract);

        BigDecimal rent = valueOrZero(
                dto.getRentAmount()
        );

        BigDecimal electricity = valueOrZero(
                dto.getElectricityAmount()
        );

        BigDecimal water = valueOrZero(
                dto.getWaterAmount()
        );

        BigDecimal fine = valueOrZero(
                dto.getFineAmount()
        );

        BigDecimal total = rent
                .add(electricity)
                .add(water)
                .add(fine);

        invoice.setTotalAmount(total);

        return invoiceRepository.save(invoice);
    }

    private BigDecimal valueOrZero(BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    // READ ALL
    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    // READ BY ID
    public Invoice getById(String id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hóa đơn"
                        )
                );
    }

    // UPDATE
    @org.springframework.transaction.annotation.Transactional
    public Invoice update(
            String id,
            InvoiceDTO dto
    ) {

        jdbc.queryForList("SELECT contract_id FROM contracts WHERE contract_id=? FOR UPDATE",dto.getContractId());
        jdbc.queryForList("SELECT invoice_id FROM invoices WHERE invoice_id=? FOR UPDATE",id);
        Invoice existing = getById(id);
        if(!java.util.Objects.equals(existing.getContract().getContractId(),dto.getContractId())||!java.util.Objects.equals(existing.getMonth(),dto.getMonth())||!java.util.Objects.equals(existing.getYear(),dto.getYear()))throw new IllegalArgumentException("Không được đổi hợp đồng hoặc kỳ của hóa đơn đã lập");
        if(!jdbc.queryForList("SELECT payment_report_id FROM payment_reports WHERE invoice_id=? AND status IN ('PENDING','PAID')",id).isEmpty())throw new IllegalArgumentException("Hóa đơn đang đối soát hoặc đã thanh toán; không được sửa số tiền");

        Contract contract = contractRepository
                .findById(dto.getContractId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hợp đồng"
                        )
                );

        existing.setMonth(dto.getMonth());
        existing.setYear(dto.getYear());
        existing.setRentAmount(dto.getRentAmount());
        existing.setElectricityAmount(
                dto.getElectricityAmount()
        );
        existing.setWaterAmount(
                dto.getWaterAmount()
        );
        existing.setFineAmount(
                dto.getFineAmount()
        );

        existing.setContract(contract);

        BigDecimal rent = valueOrZero(
                dto.getRentAmount()
        );

        BigDecimal electricity = valueOrZero(
                dto.getElectricityAmount()
        );

        BigDecimal water = valueOrZero(
                dto.getWaterAmount()
        );

        BigDecimal fine = valueOrZero(
                dto.getFineAmount()
        );

        BigDecimal total = rent
                .add(electricity)
                .add(water)
                .add(fine);

        existing.setTotalAmount(total);

        return invoiceRepository.save(existing);
    }

    // DELETE
    public void delete(String id) {

        if (!invoiceRepository.existsById(id)) {
            throw new RuntimeException(
                    "Hóa đơn không tồn tại"
            );
        }

        invoiceRepository.deleteById(id);
    }

    // GET INVOICES BY CONTRACT
    public List<Invoice> getByContract(
            String contractId
    ) {

        return invoiceRepository
                .findByContractContractId(contractId);
    }

    // TOTAL REVENUE
    public BigDecimal getTotalRevenue() {

        BigDecimal total =
                invoiceRepository.calculateTotalRevenue();

        return total == null
                ? BigDecimal.ZERO
                : total;
    }
}