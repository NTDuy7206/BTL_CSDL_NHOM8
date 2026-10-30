package com.ntd.csdl.dto;

import com.ntd.csdl.entity.PaymentReport;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentReportResponseDTO {
    private final Long paymentReportId;
    private final String username;
    private final Long userId;
    private final String invoiceId;
    private final BigDecimal amount;
    private final String roomId;
    private final String contractId;
    private Long landlordId;
    private String landlordName;
    private String routingStatus="UNASSIGNED";
    public void recipient(Long id,String name,String status){landlordId=id;landlordName=name;routingStatus=status;}
    private final String paymentMethod;
    private final String transactionReference;
    private final String status;
    private final LocalDateTime createdAt;

    public PaymentReportResponseDTO(PaymentReport report) {
        this.paymentReportId = report.getPaymentReportId();
        this.username = report.getPayer().getUsername();
        this.userId = report.getPayer().getId();
        this.invoiceId = report.getInvoice().getInvoiceId();
        this.amount = report.getAmount();
        this.roomId=report.getInvoice().getContract().getRoom().getRoomId();
        this.contractId=report.getInvoice().getContract().getContractId();
        this.paymentMethod = report.getPaymentMethod();
        this.transactionReference = report.getTransactionReference();
        this.status = report.getStatus().name();
        this.createdAt = report.getCreatedAt();
    }
}
