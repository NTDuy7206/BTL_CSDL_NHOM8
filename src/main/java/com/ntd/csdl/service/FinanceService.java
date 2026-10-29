package com.ntd.csdl.service;
import com.ntd.csdl.entity.PaymentReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
@Service @RequiredArgsConstructor
public class FinanceService {
 private final JdbcTemplate jdbc;
 private final LandlordService landlords;
 private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
 private void day(LocalDate day){if(day==null||day.isAfter(StayService.today())||day.getYear()<2000)throw bad("Ngày thực thu/chi phải từ năm 2000 và không sau hôm nay");}
 private void money(BigDecimal amount){if(amount==null||amount.signum()<=0||amount.scale()>2||amount.precision()-amount.scale()>16)throw bad("Số tiền phải dương, tối đa 16 chữ số và 2 chữ số thập phân");}
 private void period(int year,int month){if(year<2000||year>2100||month<1||month>12)throw bad("Tháng/năm không hợp lệ");}
 private void text(String value,int max,String label){if(value==null||value.isBlank()||value.length()>max)throw bad(label+" không được trống và tối đa "+max+" ký tự");}
 private void receipt(Long report,String invoice,Long owner,LocalDate date,BigDecimal amount){
  day(date);money(amount);jdbc.queryForList("SELECT invoice_id FROM invoices WHERE invoice_id=? FOR UPDATE",invoice);
  if(!jdbc.queryForList("SELECT invoice_id FROM finance_receipts WHERE invoice_id=?",invoice).isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Hóa đơn đã ghi nhận thực thu; không được thu hai lần");
  jdbc.update("INSERT INTO finance_receipts(payment_report_id,invoice_id,landlord_id,received_on,amount) VALUES (?,?,?,?,?)",report,invoice,owner,date,amount);
 }
 @Transactional public void confirmReceipt(String username,PaymentReport report,LocalDate date){receipt(report.getPaymentReportId(),report.getInvoice().getInvoiceId(),landlords.accountId(username),date==null?StayService.today():date,report.getAmount());}
 @Transactional public void allocateReceipt(String username,Long reportId,LocalDate date){
  Long owner=landlords.accountId(username);
  var rows=jdbc.queryForList("SELECT p.invoice_id,p.amount FROM payment_reports p JOIN invoices i ON i.invoice_id=p.invoice_id JOIN contracts c ON c.contract_id=i.contract_id JOIN room_landlords l ON l.room_id=c.room_id WHERE p.payment_report_id=? AND p.status='PAID' AND l.user_id=?",reportId,owner);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không có khoản thu đã xác nhận thuộc chủ nhà này");
  var row=rows.getFirst();receipt(reportId,(String)row.get("invoice_id"),owner,date,new BigDecimal(row.get("amount").toString()));
 }
 public List<Map<String,Object>> unallocated(String username){return jdbc.queryForList("SELECT p.payment_report_id AS paymentReportId,p.invoice_id AS invoiceId,p.amount,p.created_at AS reportedAt FROM payment_reports p JOIN invoices i ON i.invoice_id=p.invoice_id JOIN contracts c ON c.contract_id=i.contract_id JOIN room_landlords l ON l.room_id=c.room_id LEFT JOIN finance_receipts r ON r.invoice_id=p.invoice_id WHERE p.status='PAID' AND r.invoice_id IS NULL AND l.user_id=? ORDER BY p.created_at",landlords.accountId(username));}
 @Transactional(readOnly=true) public Map<String,Object> utilities(String username,int year,int month){
  period(year,month);Long owner=landlords.accountId(username);
  var total=jdbc.queryForMap("SELECT COUNT(*) AS invoiceCount,COALESCE(SUM(i.electricity_amount),0) AS electricity,COALESCE(SUM(i.water_amount),0) AS water,COALESCE(SUM(COALESCE(i.electricity_amount,0)+COALESCE(i.water_amount,0)),0) AS totalUtilities FROM invoices i JOIN contracts c ON c.contract_id=i.contract_id JOIN room_landlords l ON l.room_id=c.room_id WHERE l.user_id=? AND i.year=? AND i.month=?",owner,year,month);
  return Map.of("year",year,"month",month,"billed",total,"payments",jdbc.queryForList("SELECT id,bill_year AS year,bill_month AS month,utility_type AS utilityType,amount,paid_on AS paidOn,provider,reference FROM utility_disbursements WHERE landlord_id=? ORDER BY paid_on DESC,bill_year DESC,bill_month DESC",owner));
 }
 @Transactional public void payUtility(String username,int year,int month,String type,BigDecimal amount,LocalDate date,String provider,String reference){
  period(year,month);day(date);money(amount);text(provider,120,"Đơn vị cung cấp");text(reference,200,"Mã chứng từ");
  if(!Set.of("ELECTRICITY","WATER").contains(type==null?"":type))throw bad("Chọn điện hoặc nước");
  Long owner=landlords.accountId(username);jdbc.queryForList("SELECT id FROM user WHERE id=? FOR UPDATE",owner);
  if(!jdbc.queryForList("SELECT id FROM utility_disbursements WHERE landlord_id=? AND bill_year=? AND bill_month=? AND utility_type=?",owner,year,month,type).isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Kỳ điện/nước này đã ghi nhận chi trả");
  jdbc.update("INSERT INTO utility_disbursements(id,landlord_id,bill_year,bill_month,utility_type,amount,paid_on,provider,reference) VALUES (?,?,?,?,?,?,?,?,?)",UUID.randomUUID().toString(),owner,year,month,type,amount,date,provider.trim(),reference.trim());
 }
 @Transactional public void expense(String username,BigDecimal amount,LocalDate date,String description,String reference){
  day(date);money(amount);text(description,300,"Nội dung chi");text(reference,200,"Mã chứng từ");
  Long owner=landlords.accountId(username);jdbc.queryForList("SELECT id FROM user WHERE id=? FOR UPDATE",owner);
  if(!jdbc.queryForList("SELECT id FROM landlord_expenses WHERE landlord_id=? AND reference=?",owner,reference.trim()).isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Chứng từ chi phí này đã được ghi nhận");
  jdbc.update("INSERT INTO landlord_expenses(id,landlord_id,amount,paid_on,description,reference) VALUES (?,?,?,?,?,?)",UUID.randomUUID().toString(),owner,amount,date,description.trim(),reference.trim());
 }
 public List<Map<String,Object>> expenses(String username){return jdbc.queryForList("SELECT id,amount,paid_on AS paidOn,description,reference FROM landlord_expenses WHERE landlord_id=? ORDER BY paid_on DESC",landlords.accountId(username));}
 private static class Balance {
  BigDecimal income=BigDecimal.ZERO,utility=BigDecimal.ZERO,other=BigDecimal.ZERO;
  Map<String,Object> map(String period){return Map.of("period",period,"income",income,"utilityCost",utility,"otherCost",other,"expenses",utility.add(other),"profit",income.subtract(utility).subtract(other));}
 }
 private String bucket(LocalDate date,String group){return group.equals("day")?date.toString():group.equals("month")?YearMonth.from(date).toString():String.valueOf(date.getYear());}
 @Transactional(readOnly=true) public Map<String,Object> report(String username,LocalDate from,LocalDate to,String group){
  if(from==null||to==null||from.isAfter(to)||from.getYear()<2000||to.getYear()>2100||!Set.of("day","month","year").contains(group==null?"":group))throw bad("Khoảng ngày hoặc nhóm báo cáo không hợp lệ");
  long days=ChronoUnit.DAYS.between(from,to);if(days>3660||(group.equals("day")&&days>365))throw bad("Báo cáo ngày tối đa 366 ngày; tháng/năm tối đa 10 năm");
  Long owner=landlords.accountId(username);var balances=new TreeMap<String,Balance>();
  for(LocalDate d=from;!d.isAfter(to);d=d.plusDays(1))balances.putIfAbsent(bucket(d,group),new Balance());
  var total=new Balance();
  var events=jdbc.queryForList("SELECT received_on AS event_day,amount AS income,0 AS utility,0 AS other FROM finance_receipts WHERE landlord_id=? AND received_on BETWEEN ? AND ? UNION ALL SELECT paid_on,0,amount,0 FROM utility_disbursements WHERE landlord_id=? AND paid_on BETWEEN ? AND ? UNION ALL SELECT paid_on,0,0,amount FROM landlord_expenses WHERE landlord_id=? AND paid_on BETWEEN ? AND ?",owner,from,to,owner,from,to,owner,from,to);
  for(var event:events){var balance=balances.get(bucket(LocalDate.parse(event.get("event_day").toString()),group));BigDecimal income=new BigDecimal(event.get("income").toString()),utility=new BigDecimal(event.get("utility").toString()),other=new BigDecimal(event.get("other").toString());balance.income=balance.income.add(income);balance.utility=balance.utility.add(utility);balance.other=balance.other.add(other);total.income=total.income.add(income);total.utility=total.utility.add(utility);total.other=total.other.add(other);}
  return Map.of("from",from,"to",to,"group",group,"totals",total.map("total"),"rows",balances.entrySet().stream().map(e->e.getValue().map(e.getKey())).toList());
 }
 @Transactional(readOnly=true) public Map<String,Object> summary(String username,LocalDate date){if(date==null||date.getYear()<2000||date.getYear()>2100)throw bad("Ngày báo cáo không hợp lệ");return Map.of("day",report(username,date,date,"day").get("totals"),"month",report(username,date.withDayOfMonth(1),date.withDayOfMonth(date.lengthOfMonth()),"month").get("totals"),"year",report(username,date.withDayOfYear(1),date.withMonth(12).withDayOfMonth(31),"year").get("totals"));}
}
