package com.ntd.csdl.controller;
import com.ntd.csdl.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.time.LocalDate;
import java.math.BigDecimal;
@RestController @RequiredArgsConstructor @RequestMapping("/api/finance")
public class FinanceController {
 private final FinanceService service;
 public record Utility(int year,int month,String utilityType,BigDecimal amount,LocalDate paidOn,String provider,String reference){}
 public record Expense(BigDecimal amount,LocalDate paidOn,String description,String reference){}
 public record ReceiptDate(LocalDate receivedOn){}
 @GetMapping("/utilities") public Object utilities(Authentication auth,@RequestParam int year,@RequestParam int month){return service.utilities(auth.getName(),year,month);}
 @PostMapping("/utilities") public void utility(Authentication auth,@RequestBody Utility d){service.payUtility(auth.getName(),d.year(),d.month(),d.utilityType(),d.amount(),d.paidOn(),d.provider(),d.reference());}
 @GetMapping("/expenses") public Object expenses(Authentication auth){return service.expenses(auth.getName());}
 @PostMapping("/expenses") public void expense(Authentication auth,@RequestBody Expense d){service.expense(auth.getName(),d.amount(),d.paidOn(),d.description(),d.reference());}
 @GetMapping("/unallocated-receipts") public Object unallocated(Authentication auth){return service.unallocated(auth.getName());}
 @PostMapping("/receipts/{id}") public void allocate(Authentication auth,@PathVariable Long id,@RequestBody ReceiptDate d){service.allocateReceipt(auth.getName(),id,d.receivedOn());}
 @GetMapping("/summary") public Object summary(Authentication auth,@RequestParam @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date){return service.summary(auth.getName(),date);}
 @GetMapping("/report") public Object report(Authentication auth,@RequestParam @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam String group){return service.report(auth.getName(),from,to,group);}
}
