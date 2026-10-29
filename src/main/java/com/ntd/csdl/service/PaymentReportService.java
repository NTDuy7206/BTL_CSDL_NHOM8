package com.ntd.csdl.service;

import com.ntd.csdl.dto.PaymentReportCreateDTO;
import com.ntd.csdl.dto.PaymentReportResponseDTO;
import com.ntd.csdl.dto.PaymentReportStatusDTO;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.repo.InvoiceRepository;
import com.ntd.csdl.repo.PaymentReportRepository;
import com.ntd.csdl.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentReportService {
    private final PaymentReportRepository reportRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final RentalWorkflowService rentals;
    private final LandlordService landlords;

    @Transactional(readOnly = true)
    public List<PaymentReportResponseDTO> getForAccount(String username, boolean tenant) {
        List<PaymentReport> reports = tenant
                ? reportRepository.findByPayerUsernameOrderByCreatedAtDesc(username)
                : reportRepository.findAllByOrderByCreatedAtDesc();
        return reports.stream().filter(r->tenant||landlords.owns(username,r.getInvoice().getContract().getRoom().getRoomId())).map(PaymentReportResponseDTO::new).toList();
    }

    @Transactional
    public PaymentReportResponseDTO create(String username, PaymentReportCreateDTO dto) {
        String method=dto.getPaymentMethod()==null?"":dto.getPaymentMethod().trim().toUpperCase(java.util.Locale.ROOT);
        if(!List.of("BANK_TRANSFER","CASH").contains(method))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Chọn phương thức chuyển khoản hoặc tiền mặt");
        User payer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        Invoice invoice = invoiceRepository.lockById(dto.getInvoiceId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"));
        rentals.requireOwner(username, invoice.getContract());
        BigDecimal amount = invoice.getTotalAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hóa đơn chưa có số tiền hợp lệ");
        }
        if (reportRepository.existsByPayerUsernameAndInvoiceInvoiceIdAndStatusIn(
                username,
                invoice.getInvoiceId(),
                List.of(PaymentReportStatus.PENDING, PaymentReportStatus.PAID))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hóa đơn này đã có báo cáo thanh toán đang chờ hoặc đã xác nhận");
        }

        PaymentReport report = new PaymentReport();
        report.setPayer(payer);
        report.setInvoice(invoice);
        report.setAmount(amount);
        report.setPaymentMethod(method);
        report.setTransactionReference(dto.getTransactionReference().trim());
        report.setStatus(PaymentReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        return new PaymentReportResponseDTO(reportRepository.save(report));
    }

    @Transactional
    public PaymentReportResponseDTO updateStatus(Long id, PaymentReportStatusDTO dto,String username) {
        PaymentReportStatus status;
        try {
            status = PaymentReportStatus.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái chỉ được là PAID hoặc REJECTED");
        }
        if (status == PaymentReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể chuyển báo cáo về trạng thái chờ");
        }

        PaymentReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy báo cáo thanh toán"));
        landlords.requireOwner(username,report.getInvoice().getContract().getRoom().getRoomId());
        if (report.getStatus() != PaymentReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Báo cáo thanh toán này đã được xử lý");
        }
        report.setStatus(status);
        return new PaymentReportResponseDTO(report);
    }
}
