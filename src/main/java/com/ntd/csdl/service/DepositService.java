package com.ntd.csdl.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.net.URLEncoder;
import javax.imageio.ImageIO;
import java.io.*;
@Service @RequiredArgsConstructor
public class DepositService {
 private final JdbcTemplate jdbc;private final LandlordService landlords;
 private ResponseStatusException bad(String s){return new ResponseStatusException(HttpStatus.BAD_REQUEST,s);}
 public record Bank(String bankName,String accountNumber,String accountHolder,String webhookKey){}
 @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown=true)
 public record Event(Long id,String gateway,String accountNumber,String subAccount,String code,String content,String transferType,BigDecimal transferAmount){}
 private String text(String s,int max){if(s==null||s.isBlank()||s.trim().length()>max)throw bad("Điền đủ ngân hàng, số tài khoản và tên chủ tài khoản hợp lệ");return s.trim();}
 private String hash(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 private Map<String,Object> bank(long id){return jdbc.queryForList("SELECT * FROM landlord_bank_accounts WHERE user_id=?",id).stream().findFirst().orElse(Map.of());}
 public Map<String,Object> ownBank(String username){long id=landlords.accountId(username);var b=bank(id);var result=new LinkedHashMap<String,Object>();result.put("landlordId",id);for(String k:List.of("bank_name","account_number","account_holder"))result.put(k,b.get(k));result.put("configured",b.get("webhook_key_hash")!=null);result.put("hasQr",b.get("qr_image")!=null);return result;}
 @Transactional public void saveBank(String username,Bank d){long id=landlords.accountId(username);String name=text(d.bankName(),80),number=text(d.accountNumber(),40),holder=text(d.accountHolder(),120);if(!number.matches("[A-Za-z0-9]{1,40}"))throw bad("Số tài khoản chỉ gồm chữ và số");jdbc.queryForList("SELECT id FROM user WHERE id=? FOR UPDATE",id);var b=bank(id);String key=d.webhookKey()==null||d.webhookKey().isBlank()?(String)b.get("webhook_key_hash"):hash(text(d.webhookKey(),200));if(d.webhookKey()!=null&&!d.webhookKey().isBlank()&&d.webhookKey().length()<32)throw bad("Khóa webhook phải có ít nhất 32 ký tự");if(b.isEmpty())jdbc.update("INSERT INTO landlord_bank_accounts(user_id,bank_name,account_number,account_holder,webhook_key_hash) VALUES (?,?,?,?,?)",id,name,number,holder,key);else jdbc.update("UPDATE landlord_bank_accounts SET bank_name=?,account_number=?,account_holder=?,webhook_key_hash=? WHERE user_id=?",name,number,holder,key,id);}
 @Transactional public void upload(String username,MultipartFile file){long id=landlords.accountId(username);if(bank(id).isEmpty())throw bad("Lưu thông tin ngân hàng trước khi tải QR");if(file==null||file.isEmpty()||file.getSize()>5*1024*1024)throw bad("Chọn ảnh QR JPG/PNG tối đa 5 MB");try(var input=ImageIO.createImageInputStream(file.getInputStream())){var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw bad("Ảnh không hợp lệ");var reader=readers.next();try{reader.setInput(input);String f=reader.getFormatName().toLowerCase(Locale.ROOT);if(!Set.of("png","jpeg","jpg").contains(f)||(long)reader.getWidth(0)*reader.getHeight(0)>10000000)throw bad("Chọn ảnh JPG/PNG không quá 10 triệu pixel");var image=reader.read(0);var bytes=new ByteArrayOutputStream();ImageIO.write(image,"png",bytes);if(bytes.size()>5*1024*1024)throw bad("Ảnh sau xử lý vượt quá 5 MB");jdbc.update("UPDATE landlord_bank_accounts SET qr_image=? WHERE user_id=?",bytes.toByteArray(),id);}finally{reader.dispose();}}catch(IOException e){throw bad("Không đọc được ảnh QR");}}
 public byte[] ownQr(String username){byte[] bytes=(byte[])bank(landlords.accountId(username)).get("qr_image");if(bytes==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Chưa có ảnh QR");return bytes;}
 @Transactional public void initialize(long request,String room){Long owner=landlords.owner(room);BigDecimal price=jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id=?",BigDecimal.class,room);if(owner==null||price==null||price.signum()<=0||price.stripTrailingZeros().scale()>0)throw bad("Giá thuê phải là số tiền VND nguyên dương để đặt cọc");jdbc.update("INSERT INTO rental_deposits(request_id,landlord_id,amount,payment_code,status) VALUES (?,?,?,?,'REVIEW')",request,owner,price,"COC"+UUID.randomUUID().toString().replace("-","").substring(0,20).toUpperCase(Locale.ROOT));}
 public Map<String,Object> details(long request){
  var rows=jdbc.queryForList("SELECT * FROM rental_deposits WHERE request_id=?",request);if(rows.isEmpty())return Map.of();var d=rows.getFirst();var row=new LinkedHashMap<String,Object>();
  String[][] fields={{"requestId","request_id"},{"landlordId","landlord_id"},{"amount","amount"},{"paymentCode","payment_code"},{"status","status"},{"bankName","bank_name"},{"accountNumber","account_number"},{"accountHolder","account_holder"},{"paidAt","paid_at"}};
  for(var f:fields)row.put(f[0],d.get(f[1]));
  if(row.get("accountNumber")!=null){String query="acc="+enc(row.get("accountNumber"))+"&bank="+enc(row.get("bankName"))+"&amount="+enc(new BigDecimal(row.get("amount").toString()).toBigIntegerExact())+"&des="+enc(row.get("paymentCode"))+"&template=compact";row.put("qrUrl","https://vietqr.app/img?"+query);}
  return row;
 }

 private String enc(Object s){return URLEncoder.encode(s.toString(),StandardCharsets.UTF_8);}
 // Returns true only after authenticated bank confirmation. Legacy requests have no deposit record.
 @Transactional public boolean prepareApproval(long request){var rows=jdbc.queryForList("SELECT * FROM rental_deposits WHERE request_id=? FOR UPDATE",request);if(rows.isEmpty())return true;var d=rows.getFirst();if("PAID".equals(d.get("status")))return true;if("REVIEW".equals(d.get("status"))){
   var dates=jdbc.queryForMap("SELECT c.room_id,c.requested_start_date,r.requested_end_date FROM contract_requests c JOIN rental_request_details r ON r.request_id=c.request_id WHERE c.request_id=?",request);
   jdbc.queryForList("SELECT room_id FROM rooms WHERE room_id=? FOR UPDATE",dates.get("room_id"));
   if(!jdbc.queryForList("SELECT c.contract_id FROM contracts c LEFT JOIN contract_stays s ON s.contract_id=c.contract_id WHERE c.room_id=? AND (COALESCE(s.check_in,c.start_date)<=? AND ((s.check_out IS NULL AND c.end_date>=?) OR s.check_out>?) OR s.check_out IS NULL AND (COALESCE(s.check_in,c.start_date) IS NULL OR c.end_date IS NULL))",dates.get("room_id"),dates.get("requested_end_date"),dates.get("requested_start_date"),dates.get("requested_start_date")).isEmpty())throw bad("Phòng đã có hợp đồng trong thời gian này; không yêu cầu chuyển cọc");
   if(!jdbc.queryForList("SELECT d.request_id FROM rental_deposits d JOIN contract_requests c ON c.request_id=d.request_id JOIN rental_request_details r ON r.request_id=c.request_id WHERE c.room_id=? AND d.request_id<>? AND d.status IN ('AWAITING_PAYMENT','PAID') AND c.status='PENDING' AND c.requested_start_date<=? AND r.requested_end_date>=?",dates.get("room_id"),request,dates.get("requested_end_date"),dates.get("requested_start_date")).isEmpty())throw bad("Phòng đang có yêu cầu cọc khác trong thời gian này; không thu thêm cọc chồng lịch");
   var b=bank(((Number)d.get("landlord_id")).longValue());if(b.get("webhook_key_hash")==null)throw bad("Chủ nhà cần lưu ngân hàng và kết nối webhook SePay trong Thông tin cá nhân trước khi yêu cầu cọc");jdbc.update("UPDATE rental_deposits SET status='AWAITING_PAYMENT',bank_name=?,account_number=?,account_holder=? WHERE request_id=?",b.get("bank_name"),b.get("account_number"),b.get("account_holder"),request);}return false;}
 public BigDecimal amount(long request){return jdbc.queryForList("SELECT amount FROM rental_deposits WHERE request_id=?",request).stream().map(r->new BigDecimal(r.get("amount").toString())).findFirst().orElse(BigDecimal.ZERO);}
 @Transactional public void reject(long request){var rows=jdbc.queryForList("SELECT status FROM rental_deposits WHERE request_id=? FOR UPDATE",request);if(!rows.isEmpty()&&"PAID".equals(rows.getFirst().get("status")))throw bad("Đã nhận cọc. Cần đối soát và hoàn cọc trước khi hủy; hệ thống không tự hoàn tiền");jdbc.update("UPDATE rental_deposits SET status='CANCELLED' WHERE request_id=?",request);}
 @Transactional public Map<String,Object> webhook(long owner,String authorization,Event e){
  jdbc.queryForList("SELECT id FROM user WHERE id=? FOR UPDATE",owner);var b=bank(owner);String expected=(String)b.get("webhook_key_hash");String key=authorization!=null&&authorization.startsWith("Apikey ")?authorization.substring(7):"";if(expected==null||key.isBlank()||!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),hash(key).getBytes(StandardCharsets.US_ASCII)))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Webhook không hợp lệ");
  if(e==null||e.id()==null||e.id()<=0||e.transferAmount()==null||e.transferAmount().signum()<=0)throw bad("Dữ liệu giao dịch không hợp lệ");
  if(!jdbc.queryForList("SELECT provider_id FROM deposit_bank_events WHERE landlord_id=? AND provider_id=?",owner,e.id()).isEmpty())return Map.of("success",true,"result","DUPLICATE");
  String content=((e.code()==null?"":e.code())+" "+(e.content()==null?"":e.content())).toUpperCase(Locale.ROOT);var matcher=java.util.regex.Pattern.compile("(?<![A-Z0-9])COC[A-F0-9]{20}(?![A-Z0-9])").matcher(content);var codes=new HashSet<String>();while(matcher.find())codes.add(matcher.group());String code=codes.size()==1?codes.iterator().next():"";
  var rows=jdbc.queryForList("SELECT * FROM rental_deposits WHERE landlord_id=? AND payment_code=? FOR UPDATE",owner,code);Long request=null;String result="UNMATCHED";
  if(!rows.isEmpty()){var d=rows.getFirst();request=((Number)d.get("request_id")).longValue();boolean account=Objects.equals(d.get("account_number"),e.accountNumber())&&e.gateway()!=null&&d.get("bank_name").toString().equalsIgnoreCase(e.gateway().trim());if("in".equals(e.transferType())&&account&&"AWAITING_PAYMENT".equals(d.get("status"))&&new BigDecimal(d.get("amount").toString()).compareTo(e.transferAmount())==0){jdbc.update("UPDATE rental_deposits SET status='PAID',paid_at=? WHERE request_id=?",LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")),request);result="PAID";}else result="NEEDS_REVIEW";}
  jdbc.update("INSERT INTO deposit_bank_events(landlord_id,provider_id,request_id,amount,result,received_at) VALUES (?,?,?,?,?,?)",owner,e.id(),request,e.transferAmount(),result,LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));return Map.of("success",true,"result",result);
 }
 public java.util.List<Map<String,Object>> events(String username){return jdbc.queryForList("SELECT provider_id,request_id,amount,result,received_at FROM deposit_bank_events WHERE landlord_id=? ORDER BY received_at DESC",landlords.accountId(username));}
}
