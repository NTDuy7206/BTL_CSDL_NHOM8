package com.ntd.csdl.repo;

import com.ntd.csdl.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceRepository
        extends JpaRepository<Invoice, String> {

    // Derived Query
    List<Invoice> findByContractContractId(String contractId);

    List<Invoice> findByMonthAndYear(
            Integer month,
            Integer year
    );

    List<Invoice> findByTotalAmountGreaterThan(
            BigDecimal amount
    );

    // JPQL
    @Query("""
        SELECT i
        FROM Invoice i
        WHERE i.month = :month
        AND i.year = :year
    """)
    List<Invoice> findInvoicesByMonthAndYear(
            Integer month,
            Integer year
    );

    // Truy vấn nhiều bảng
    @Query("""
        SELECT i
        FROM Invoice i
        JOIN FETCH i.contract c
        JOIN FETCH c.room
        JOIN FETCH c.tenant
        WHERE i.invoiceId = :invoiceId
    """)
    Invoice findInvoiceDetail(String invoiceId);

    // Thống kê tổng doanh thu
    @Query("""
        SELECT SUM(i.totalAmount)
        FROM Invoice i
    """)
    BigDecimal calculateTotalRevenue();

    // Thống kê doanh thu theo tháng
    @Query("""
        SELECT i.month,
               i.year,
               SUM(i.totalAmount)
        FROM Invoice i
        GROUP BY i.month, i.year
        ORDER BY i.year, i.month
    """)
    List<Object[]> revenueByMonth();
}