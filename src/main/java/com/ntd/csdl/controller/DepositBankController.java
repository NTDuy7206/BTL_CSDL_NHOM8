package com.ntd.csdl.controller;
import com.ntd.csdl.service.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import java.util.*;
@RestController @RequiredArgsConstructor
public class DepositBankController {
 private final DepositService service;
 @GetMapping("/api/bank") public Map<String,Object> bank(Authentication a){return service.ownBank(a.getName());}
 @PutMapping("/api/bank") public void save(Authentication a,@RequestBody DepositService.Bank d){service.saveBank(a.getName(),d);}
 @PostMapping("/api/bank/qr") public void upload(Authentication a,@RequestParam("file") MultipartFile file){service.upload(a.getName(),file);}
 @GetMapping("/api/bank/qr") public ResponseEntity<byte[]> qr(Authentication a){return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).header("Cache-Control","no-store").body(service.ownQr(a.getName()));}
 @GetMapping("/api/bank/events") public List<Map<String,Object>> events(Authentication a){return service.events(a.getName());}
 public record TransferReport(String bankReference) {}
 @GetMapping("/api/deposit-invoices")
 public List<Map<String,Object>> invoices(Authentication auth) {
  boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  return service.invoices(auth.getName(), admin);
 }
 @PostMapping("/api/deposit-invoices/{id}/report")
 public void report(Authentication auth, @PathVariable long id, @RequestBody TransferReport report) {
  service.reportTransfer(auth.getName(), id, report.bankReference());
 }
 @PostMapping("/api/deposit-invoices/{id}/confirm")
 public void confirm(Authentication auth, @PathVariable long id) {
  service.confirmReceived(auth.getName(), id);
 }
 @PostMapping("/api/deposit-webhooks/sepay/{owner}") public Map<String,Object> webhook(@PathVariable long owner,@RequestHeader(value="Authorization",required=false)String key,@RequestBody DepositService.Event event){return service.webhook(owner,key,event);}
}
