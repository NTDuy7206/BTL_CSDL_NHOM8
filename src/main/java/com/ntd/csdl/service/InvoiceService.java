package com.ntd.csdl.service;

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

    // CREATE INVOICE
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

    // READ
    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    public Invoice getById(String id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hóa đơn"
                        ));
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

    // Hóa đơn theo hợp đồng
    public List<Invoice> getByContract(
            String contractId
    ) {

        return invoiceRepository
                .findByContractContractId(contractId);
    }

    // Tổng doanh thu
    public BigDecimal getTotalRevenue() {

        BigDecimal total =
                invoiceRepository.calculateTotalRevenue();

        return total == null
                ? BigDecimal.ZERO
                : total;
    }
}