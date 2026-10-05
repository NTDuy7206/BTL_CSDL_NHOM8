package com.ntd.csdl.service;

import com.ntd.csdl.dto.InvoiceDTO;
import com.ntd.csdl.entity.Contract;
import com.ntd.csdl.entity.Invoice;
import com.ntd.csdl.repo.ContractRepository;
import com.ntd.csdl.repo.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;

    // CREATE
    @CacheEvict(
            value = {
                    "invoices",
                    "invoiceById",
                    "invoicesByContract",
                    "totalRevenue"
            },
            allEntries = true
    )
    public Invoice create(InvoiceDTO dto) {

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
    @Cacheable(value = "invoices")
    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    // READ BY ID
    @Cacheable(value = "invoiceById", key = "#id")
    public Invoice getById(String id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hóa đơn"
                        )
                );
    }

    // UPDATE
    @CacheEvict(
            value = {
                    "invoices",
                    "invoiceById",
                    "invoicesByContract",
                    "totalRevenue"
            },
            allEntries = true
    )
    public Invoice update(
            String id,
            InvoiceDTO dto
    ) {

        Invoice existing = getById(id);

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
    @CacheEvict(
            value = {
                    "invoices",
                    "invoiceById",
                    "invoicesByContract",
                    "totalRevenue"
            },
            allEntries = true
    )
    public void delete(String id) {

        if (!invoiceRepository.existsById(id)) {
            throw new RuntimeException(
                    "Hóa đơn không tồn tại"
            );
        }

        invoiceRepository.deleteById(id);
    }

    // GET INVOICES BY CONTRACT
    @Cacheable(
            value = "invoicesByContract",
            key = "#contractId"
    )
    public List<Invoice> getByContract(
            String contractId
    ) {

        return invoiceRepository
                .findByContractContractId(contractId);
    }

    // TOTAL REVENUE
    @Cacheable(value = "totalRevenue")
    public BigDecimal getTotalRevenue() {

        BigDecimal total =
                invoiceRepository.calculateTotalRevenue();

        return total == null
                ? BigDecimal.ZERO
                : total;
    }
}