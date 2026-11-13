package com.ntd.csdl.service;
import java.math.*;import java.time.*;import java.time.temporal.ChronoUnit;
public final class BillingMath {
 private BillingMath(){}
 public static BigDecimal nightlyRent(BigDecimal rate, LocalDate arrival, LocalDate departure) {
  if(rate==null||rate.signum()<0)throw new IllegalArgumentException("Thiếu giá thuê hợp lệ");
  return rate.multiply(BigDecimal.valueOf(Math.max(0,ChronoUnit.DAYS.between(arrival,departure))))
      .setScale(2,RoundingMode.HALF_UP);
 }
 static BigDecimal nightlyRentForPeriod(BigDecimal rate, LocalDate arrival, LocalDate departure, YearMonth period) {
  LocalDate from = arrival.isAfter(period.atDay(1)) ? arrival : period.atDay(1);
  LocalDate nextMonth = period.plusMonths(1).atDay(1);
  LocalDate until = departure.isBefore(nextMonth) ? departure : nextMonth;
  return nightlyRent(rate, from, until);
 }
 public static BigDecimal rent(BigDecimal monthly,LocalDate start,LocalDate inclusiveEnd,YearMonth month){if(monthly==null||monthly.signum()<0)throw new IllegalArgumentException("Thiếu giá thuê hợp lệ");LocalDate a=start.isAfter(month.atDay(1))?start:month.atDay(1),b=inclusiveEnd.isBefore(month.atEndOfMonth())?inclusiveEnd:month.atEndOfMonth();long days=Math.max(0,ChronoUnit.DAYS.between(a,b)+1);return monthly.multiply(BigDecimal.valueOf(days)).divide(BigDecimal.valueOf(month.lengthOfMonth()),2,RoundingMode.HALF_UP);}
 public static BigDecimal usage(Integer start,Integer end,BigDecimal price,String label){if(start==null||end==null||start<0||end<start||price==null||price.signum()<0)throw new IllegalArgumentException("Thiếu hoặc sai chỉ số/đơn giá "+label);return price.multiply(BigDecimal.valueOf((long)end-start)).setScale(2,RoundingMode.HALF_UP);}
}
