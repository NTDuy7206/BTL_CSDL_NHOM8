package com.ntd.csdl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VipRentalService {
    private final JdbcTemplate jdbc;
    private final LandlordService landlords;
    private static final Pattern VIP_TYPE = Pattern.compile("\\bVIP\\b", Pattern.CASE_INSENSITIVE);

    public record Quote(BigDecimal monthlyRent, BigDecimal totalRent, BigDecimal depositAmount) {
        public BigDecimal getBalance() { return totalRent.subtract(depositAmount); }
    }

    public static boolean isVip(String type) {
        return type != null && VIP_TYPE.matcher(type).find();
    }

    static Quote calculate(BigDecimal rate, LocalDate start, LocalDate end) {
        BigDecimal total = BillingMath.nightlyRent(rate, start, end);
        return new Quote(rate, total, total.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
    }

    public Quote quote(String roomId, LocalDate start, LocalDate end) {
        RentalWorkflowService.dates(start, end);
        if (start.isBefore(StayService.today()) || end.getYear() > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày thuê phải từ hôm nay đến hết năm 2100");
        }
        landlords.requireRentable(roomId);
        var room = jdbc.queryForMap("SELECT room_type,base_price FROM rooms WHERE room_id=?", roomId);
        if (!isVip((String)room.get("room_type"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ phòng VIP áp dụng đặt cọc 50%");
        }
        var rate = new BigDecimal(room.get("base_price").toString());
        return calculate(rate, start, end);
    }

    @Transactional
    public String simulate(String username, String roomId, LocalDate start, LocalDate end, BigDecimal expectedDeposit) {
        long accountId = landlords.accountId(username);
        jdbc.queryForList("SELECT id FROM user WHERE id=? FOR UPDATE", accountId);
        Quote quote = quote(roomId, start, end);
        if (expectedDeposit == null || expectedDeposit.compareTo(quote.depositAmount()) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số tiền cọc đã thay đổi. Vui lòng tải lại báo giá");
        }
        var existing = jdbc.queryForList("""
                SELECT deposit_id FROM vip_rental_deposits
                WHERE user_id=? AND room_id=? AND start_date=? AND end_date=? AND monthly_rent=? AND total_rent=? AND request_id IS NULL
                """, String.class, accountId, roomId, start, end, quote.monthlyRent(), quote.totalRent());
        if (!existing.isEmpty()) return existing.getFirst();

        String id = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO vip_rental_deposits(deposit_id,user_id,room_id,start_date,end_date,monthly_rent,total_rent,deposit_amount,paid_at)
                VALUES (?,?,?,?,?,?,?,?,?)
                """, id, accountId, roomId, start, end, quote.monthlyRent(), quote.totalRent(), quote.depositAmount(),
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        return id;
    }

    public void attach(String token, long userId, String roomId, LocalDate start, LocalDate end, long requestId, BigDecimal rate) {
        Quote quote = calculate(rate, start, end);
        if (token == null || token.length() != 36 || jdbc.update("""
                UPDATE vip_rental_deposits SET request_id=?
                WHERE deposit_id=? AND user_id=? AND room_id=? AND start_date=? AND end_date=? AND request_id IS NULL
                AND monthly_rent=? AND total_rent=?
                """, requestId, token, userId, roomId, start, end, rate, quote.totalRent()) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phòng VIP cần đặt cọc 50% cho đúng phòng và thời hạn trước khi gửi yêu cầu");
        }
    }

    public Quote forRequest(long requestId) {
        return jdbc.query("SELECT monthly_rent,total_rent,deposit_amount FROM vip_rental_deposits WHERE request_id=?",
                (rs, index) -> new Quote(rs.getBigDecimal(1), rs.getBigDecimal(2), rs.getBigDecimal(3)), requestId)
                .stream().findFirst().orElse(null);
    }

    static BigDecimal balanceForContract(JdbcTemplate jdbc, String contractId) {
        return jdbc.query("""
                SELECT v.total_rent-v.deposit_amount FROM vip_rental_deposits v
                JOIN rental_request_details d ON d.request_id=v.request_id WHERE d.contract_id=?
                """, (rs, index) -> rs.getBigDecimal(1), contractId).stream().findFirst().orElse(null);
    }
}
