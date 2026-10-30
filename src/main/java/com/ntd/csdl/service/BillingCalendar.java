package com.ntd.csdl.service;
import java.time.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
public final class BillingCalendar {
 private BillingCalendar(){}
 public static boolean due(Integer year,Integer month,LocalDate today){return year!=null&&month!=null&&year>=2000&&year<=2100&&month>=1&&month<=12&&!today.isBefore(YearMonth.of(year,month).atEndOfMonth());}
 public static void requireDue(Integer year,Integer month,LocalDate today){if(!due(year,month,today))throw new ResponseStatusException(HttpStatus.CONFLICT,"Hóa đơn chưa đến ngày cuối tháng hoặc thiếu kỳ hợp lệ; chưa được thanh toán");}
 public static YearMonth latestDue(LocalDate today){YearMonth m=YearMonth.from(today);return today.equals(m.atEndOfMonth())?m:m.minusMonths(1);}
}
