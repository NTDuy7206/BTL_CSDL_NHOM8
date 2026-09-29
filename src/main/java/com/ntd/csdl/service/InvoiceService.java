package com.ntd.csdl.service;

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

    // CREATE INVOICE
    @CacheEvict(
            value = {
                    "invoices",
                    "invoiceById",
                    "invoicesByContract",
                    "totalRevenue"
            },
            allEntries = true
    )
    public Invoice create(
            String contractId,
            Invoice invoice
    ) {

        Contract contract = contractRepository
                .findById(contractId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hợp đồng"
                        ));

        invoice.setContract(contract);

        BigDecimal rent = valueOrZero(
                invoice.getRentAmount()
        );

        BigDecimal electricity = valueOrZero(
                invoice.getElectricityAmount()
        );

        BigDecimal water = valueOrZero(
                invoice.getWaterAmount()
        );

        BigDecimal fine = valueOrZero(
                invoice.getFineAmount()
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

    // READ - lấy tất cả hóa đơn
    @Cacheable(value = "invoices")
    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    // READ - lấy hóa đơn theo ID
    @Cacheable(value = "invoiceById", key = "#id")
    public Invoice getById(String id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hóa đơn"
                        ));
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

    // Hóa đơn theo hợp đồng
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

    // Tổng doanh thu
    @Cacheable(value = "totalRevenue")
    public BigDecimal getTotalRevenue() {

        BigDecimal total =
                invoiceRepository.calculateTotalRevenue();

        return total == null
                ? BigDecimal.ZERO
                : total;
    }
}