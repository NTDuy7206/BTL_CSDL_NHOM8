package com.ntd.csdl.service;
import java.math.*;import java.time.*;import java.time.temporal.ChronoUnit;
public final class BillingMath {
 private BillingMath(){}
 public static BigDecimal rent(BigDecimal monthly,LocalDate start,LocalDate inclusiveEnd,YearMonth month){if(monthly==null||monthly.signum()<0)throw new IllegalArgumentException("Thiếu giá thuê hợp lệ");LocalDate a=start.isAfter(month.atDay(1))?start:month.atDay(1),b=inclusiveEnd.isBefore(month.atEndOfMonth())?inclusiveEnd:month.atEndOfMonth();long days=Math.max(0,ChronoUnit.DAYS.between(a,b)+1);return monthly.multiply(BigDecimal.valueOf(days)).divide(BigDecimal.valueOf(month.lengthOfMonth()),2,RoundingMode.HALF_UP);}
 public static BigDecimal usage(Integer start,Integer end,BigDecimal price,String label){if(start==null||end==null||start<0||end<start||price==null||price.signum()<0)throw new IllegalArgumentException("Thiếu hoặc sai chỉ số/đơn giá "+label);return price.multiply(BigDecimal.valueOf((long)end-start)).setScale(2,RoundingMode.HALF_UP);}
}
