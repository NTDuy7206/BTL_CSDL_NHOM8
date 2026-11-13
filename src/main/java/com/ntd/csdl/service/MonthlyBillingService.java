package com.ntd.csdl.service;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.jdbc.core.JdbcTemplate;import java.util.*;import java.time.*;import java.math.*;
@Service @RequiredArgsConstructor public class MonthlyBillingService {
 private final JdbcTemplate jdbc;
 static Integer number(Object v){return v==null?null:((Number)v).intValue();}static BigDecimal money(Object v){return v==null?null:new BigDecimal(v.toString());}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
 public String billDue(String contractId, YearMonth month, LocalDate today) {
  var contracts = jdbc.queryForList("SELECT * FROM contracts WHERE contract_id=? FOR UPDATE", contractId);
  if (contracts.isEmpty()) return "SKIPPED";
  var contract = contracts.getFirst();
  LocalDate end = StayService.date(contract.get("end_date"));
  BigDecimal nightlyRate = StayService.nightlyRate(jdbc, contractId);
  if (nightlyRate != null && month.atEndOfMonth().isAfter(today)) return "NOT_DUE";
  if (nightlyRate == null && !BillingCalendar.rentalDue(month.getYear(), month.getMonthValue(), end, today)) return "NOT_DUE";

  BigDecimal vipBalance = VipRentalService.balanceForContract(jdbc, contractId);
  BigDecimal vipRent = vipRent(contractId, month, end, nightlyRate, vipBalance);
  String result = bill(contractId, month, vipRent, nightlyRate);
  boolean vipFinal = vipRent != null && vipRent.signum() > 0;
  if (!"NEEDS_READING".equals(result) && !(vipFinal && "SKIPPED".equals(result))) return result;
  if (!jdbc.queryForList("SELECT invoice_id FROM invoices WHERE contract_id=? AND year=? AND month=?",
      contractId, month.getYear(), month.getMonthValue()).isEmpty()) return result;

  var stays = jdbc.queryForList("SELECT * FROM contract_stays WHERE contract_id=?", contractId);
  if (stays.isEmpty() && !vipFinal) return result;
  var stay = stays.isEmpty() ? Map.<String,Object>of() : stays.getFirst();
  BigDecimal monthlyRent = money(stay.get("monthly_rent"));
  if (!vipFinal && nightlyRate == null && (monthlyRent == null || monthlyRent.signum() <= 0)) return result;

  LocalDate start = StayService.date(stay.get("check_in"));
  if (start == null) start = StayService.date(contract.get("start_date"));
  LocalDate checkout = StayService.date(stay.get("check_out"));
  if (checkout != null) end = nightlyRate == null ? checkout.minusDays(1) : checkout;
  if (start == null || end == null) return result;
  BigDecimal rent = vipRent != null ? vipRent : nightlyRate != null
      ? BillingMath.nightlyRentForPeriod(nightlyRate, start, end, month) : BillingMath.rent(monthlyRent, start, end, month);
  if (rent.signum() <= 0) return result;

  // Leave the period open for meter data until a payment report is submitted.
  String invoiceId = RentalWorkflowService.id("TD");
  jdbc.update("""
      INSERT INTO invoices(invoice_id,month,year,rent_amount,electricity_amount,water_amount,fine_amount,total_amount,contract_id)
      VALUES (?,?,?,?,NULL,NULL,0,?,?)
      """, invoiceId, month.getMonthValue(), month.getYear(), rent, rent, contractId);
  notifyTenant(contract.get("tenant_id"), month, invoiceId);
  return "CREATED";
 }
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
 public String bill(String id,YearMonth month){
  BigDecimal balance=VipRentalService.balanceForContract(jdbc,id);
  BigDecimal rate=StayService.nightlyRate(jdbc,id);
  if(balance==null)return bill(id,month,null,rate);
  LocalDate end=jdbc.queryForObject("SELECT end_date FROM contracts WHERE contract_id=? FOR UPDATE",LocalDate.class,id);
  return bill(id,month,vipRent(id,month,end,rate,balance),rate);
 }

 private BigDecimal vipRent(String id, YearMonth month, LocalDate end, BigDecimal rate, BigDecimal balance) {
  if (balance == null || end == null) return null;
  if (rate != null) {
   var checkout = jdbc.queryForList("SELECT check_out FROM contract_stays WHERE contract_id=?", LocalDate.class,id)
       .stream().filter(Objects::nonNull).findFirst().orElse(end);
   end = checkout.minusDays(1);
  }
  return month.equals(YearMonth.from(end)) ? balance : BigDecimal.ZERO;
 }

 private String bill(String id,YearMonth month,BigDecimal vipRent,BigDecimal nightlyRate){
  var rows=jdbc.queryForList("SELECT c.*,r.base_price FROM contracts c JOIN rooms r ON r.room_id=c.room_id WHERE c.contract_id=? FOR UPDATE",id);if(rows.isEmpty())return "SKIPPED";var c=rows.getFirst();
  if(!jdbc.queryForList("SELECT invoice_id FROM automatic_bills WHERE contract_id=? AND year=? AND month=?",id,month.getYear(),month.getMonthValue()).isEmpty())return "EXISTS";
  var stays=jdbc.queryForList("SELECT * FROM contract_stays WHERE contract_id=?",id);Map<String,Object> stay=stays.isEmpty()?Map.of():stays.getFirst();LocalDate start=StayService.date(stay.get("check_in"));if(start==null)start=StayService.date(c.get("start_date"));LocalDate checkout=StayService.date(stay.get("check_out")),end=checkout==null?StayService.date(c.get("end_date")):checkout.minusDays(1);
  if(nightlyRate!=null&&checkout==null&&end!=null)end=end.minusDays(1);
  if(start==null||end==null||start.isAfter(month.atEndOfMonth())||end.isBefore(month.atDay(1))||end.isBefore(start)){clear(id,month);return "SKIPPED";}
  var old=jdbc.queryForList("SELECT * FROM invoices WHERE contract_id=? AND year=? AND month=? FOR UPDATE",id,month.getYear(),month.getMonthValue());
  try{
   if(old.size()>1)throw new IllegalArgumentException("Có nhiều hóa đơn cùng kỳ; cần chủ nhà kiểm tra");
   if(!old.isEmpty()){
    var invoice=old.getFirst();String existing=(String)invoice.get("invoice_id");
    // A manually prepared or paid invoice is authoritative. Do not generate another invoice or alter it.
    if(!jdbc.queryForList("SELECT payment_report_id FROM payment_reports WHERE invoice_id=? AND status IN ('PENDING','PAID')",existing).isEmpty()||Optional.ofNullable(money(invoice.get("electricity_amount"))).orElse(BigDecimal.ZERO).signum()!=0||Optional.ofNullable(money(invoice.get("water_amount"))).orElse(BigDecimal.ZERO).signum()!=0){mark(id,month,existing);if(invoice.get("electricity_amount")!=null||invoice.get("water_amount")!=null)clear(id,month);return "MANUAL";}
   }
   var readings=jdbc.queryForList("SELECT * FROM utility_readings WHERE room_id=? AND year=? AND month=? FOR UPDATE",c.get("room_id"),month.getYear(),month.getMonthValue());if(readings.size()!=1)throw new IllegalArgumentException(readings.isEmpty()?"Chưa có chỉ số điện nước của tháng":"Có nhiều bản chỉ số điện nước cùng kỳ");var r=readings.getFirst();
   BigDecimal electricity,water;
   if(!jdbc.queryForList("SELECT reading_id FROM simulated_meter_periods WHERE reading_id=?",r.get("reading_id")).isEmpty()){
    int days=BillingMath.nightlyRentForPeriod(BigDecimal.ONE,start,end.plusDays(1),month).intValueExact();
    electricity=BillingMath.usage(0,days*MeterSimulationService.ELECTRICITY_PER_DAY,money(r.get("electricity_price")),"điện");
    water=BillingMath.usage(0,days*MeterSimulationService.WATER_PER_DAY,money(r.get("water_price")),"nước");
   }else{
   if(stay.get("check_in")==null)throw new IllegalArgumentException("Chưa xác nhận ngày và chỉ số check-in thực tế");
   BillingMath.usage(number(r.get("electricity_start_index")),number(r.get("electricity_end_index")),money(r.get("electricity_price")),"điện tháng");BillingMath.usage(number(r.get("water_start_index")),number(r.get("water_end_index")),money(r.get("water_price")),"nước tháng");
   Integer es=number(r.get("electricity_start_index")),ee=number(r.get("electricity_end_index")),ws=number(r.get("water_start_index")),we=number(r.get("water_end_index"));
   if(start.isAfter(month.atDay(1))){es=number(stay.get("check_in_electric"));ws=number(stay.get("check_in_water"));}
   else if(start.equals(month.atDay(1))&&stay.get("check_in_electric")!=null){es=number(stay.get("check_in_electric"));ws=number(stay.get("check_in_water"));}
   if(end.isBefore(month.atEndOfMonth())){ee=number(stay.get("check_out_electric"));we=number(stay.get("check_out_water"));}
   else if(checkout!=null&&checkout.equals(month.plusMonths(1).atDay(1))){ee=number(stay.get("check_out_electric"));we=number(stay.get("check_out_water"));}
   electricity=BillingMath.usage(es,ee,money(r.get("electricity_price")),"điện (kWh)");water=BillingMath.usage(ws,we,money(r.get("water_price")),"nước (m³)");
   if(es<number(r.get("electricity_start_index"))||ee>number(r.get("electricity_end_index"))||ws<number(r.get("water_start_index"))||we>number(r.get("water_end_index")))throw new IllegalArgumentException("Chỉ số nhận/trả phòng nằm ngoài chỉ số tháng");
   }
   BigDecimal rent=vipRent!=null?vipRent:!old.isEmpty()&&old.getFirst().get("rent_amount")!=null?money(old.getFirst().get("rent_amount")):nightlyRate!=null?BillingMath.nightlyRentForPeriod(nightlyRate,start,end.plusDays(1),month):BillingMath.rent(money(stay.get("monthly_rent")),start,end,month),fine=BigDecimal.ZERO;String invoiceId;
   if(vipRent!=null&&old.isEmpty()&&rent.add(electricity).add(water).signum()==0){clear(id,month);return "SKIPPED";}
   if(old.isEmpty()){invoiceId=RentalWorkflowService.id("TD");jdbc.update("INSERT INTO invoices(invoice_id,month,year,rent_amount,electricity_amount,water_amount,fine_amount,total_amount,contract_id) VALUES (?,?,?,?,?,?,?,?,?)",invoiceId,month.getMonthValue(),month.getYear(),rent,electricity,water,fine,rent.add(electricity).add(water),id);}
   else{var oldInvoice=old.getFirst();invoiceId=(String)oldInvoice.get("invoice_id");if(oldInvoice.get("rent_amount")!=null)rent=money(oldInvoice.get("rent_amount"));fine=Optional.ofNullable(money(oldInvoice.get("fine_amount"))).orElse(BigDecimal.ZERO);jdbc.update("UPDATE invoices SET rent_amount=?,electricity_amount=?,water_amount=?,total_amount=? WHERE invoice_id=?",rent,electricity,water,rent.add(electricity).add(water).add(fine),invoiceId);}
   mark(id,month,invoiceId);clear(id,month);
   notifyTenant(c.get("tenant_id"), month, invoiceId);
   return "CREATED";
  }catch(IllegalArgumentException e){var alert=jdbc.queryForList("SELECT contract_id FROM billing_alerts WHERE contract_id=? AND year=? AND month=?",id,month.getYear(),month.getMonthValue());if(alert.isEmpty())jdbc.update("INSERT INTO billing_alerts(contract_id,year,month,message) VALUES (?,?,?,?)",id,month.getYear(),month.getMonthValue(),e.getMessage());else jdbc.update("UPDATE billing_alerts SET message=? WHERE contract_id=? AND year=? AND month=?",e.getMessage(),id,month.getYear(),month.getMonthValue());return "NEEDS_READING";}
 }
 private void notifyTenant(Object tenantId, YearMonth month, String invoiceId) {
  var accounts = jdbc.queryForList("SELECT user_id FROM tenant_accounts WHERE tenant_id=?", tenantId);
  if (accounts.size() != 1 || !jdbc.queryForList("SELECT invoice_id FROM invoice_notifications WHERE invoice_id=?", invoiceId).isEmpty()) return;
  jdbc.update("INSERT INTO invoice_notifications(invoice_id,user_id,message,seen) VALUES (?,?,?,false)", invoiceId,
      accounts.getFirst().get("user_id"), "Hóa đơn tháng " + month.getMonthValue() + "/" + month.getYear() + " đã sẵn sàng: " + invoiceId);
 }
 private void mark(String id,YearMonth m,String invoice){jdbc.update("INSERT INTO automatic_bills(contract_id,year,month,invoice_id) VALUES (?,?,?,?)",id,m.getYear(),m.getMonthValue(),invoice);}
 private void clear(String id,YearMonth m){jdbc.update("DELETE FROM billing_alerts WHERE contract_id=? AND year=? AND month=?",id,m.getYear(),m.getMonthValue());}
}
