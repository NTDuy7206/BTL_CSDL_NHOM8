package com.ntd.csdl.service;
import java.time.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
public final class BillingCalendar {
 private BillingCalendar(){}
 public static boolean due(Integer year,Integer month,LocalDate today){return year!=null&&month!=null&&year>=2000&&year<=2100&&month>=1&&month<=12&&!today.isBefore(YearMonth.of(year,month).atEndOfMonth());}
 public static void requireDue(Integer year,Integer month,LocalDate today){if(!due(year,month,today))throw new ResponseStatusException(HttpStatus.CONFLICT,"Hóa đơn chưa đến ngày cuối tháng hoặc thiếu kỳ hợp lệ; chưa được thanh toán");}
 public static boolean rentalDue(Integer year, Integer month, LocalDate contractEnd, LocalDate today) {
  if (year == null || month == null || year < 2000 || year > 2100 || month < 1 || month > 12) return false;
  YearMonth period = YearMonth.of(year, month);
  LocalDate dueDate = contractEnd != null && YearMonth.from(contractEnd).equals(period)
      ? contractEnd : period.atEndOfMonth();
  return !today.isBefore(dueDate);
 }

 public static boolean rentalDue(com.ntd.csdl.entity.Invoice invoice, LocalDate today) {
  return rentalDue(invoice.getYear(), invoice.getMonth(), invoice.getContract().getEndDate(), today);
 }
 public static YearMonth latestDue(LocalDate today){YearMonth m=YearMonth.from(today);return today.equals(m.atEndOfMonth())?m:m.minusMonths(1);}
}
