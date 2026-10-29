package com.ntd.csdl.controller;
import com.ntd.csdl.service.StayService;import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;import java.time.LocalDate;import java.util.Map;
@RestController @RequiredArgsConstructor @RequestMapping("/api/contracts") public class StayController{
 private final StayService stays;public record ActualStay(LocalDate date,Integer electricityIndex,Integer waterIndex){}
 public record Rate(java.math.BigDecimal monthlyRent){}
 @PostMapping("/{id}/rent-rate") public void rate(@PathVariable String id,@RequestBody Rate dto,Authentication auth){stays.setRent(id,auth.getName(),dto.monthlyRent());}
 @PostMapping("/{id}/check-in") public Map<String,Object> checkIn(@PathVariable String id,@RequestBody ActualStay dto,Authentication auth){return stays.record(id,auth.getName(),true,dto.date(),dto.electricityIndex(),dto.waterIndex());}
 @PostMapping("/{id}/check-out") public Map<String,Object> checkOut(@PathVariable String id,@RequestBody ActualStay dto,Authentication auth){return stays.record(id,auth.getName(),false,dto.date(),dto.electricityIndex(),dto.waterIndex());}
}
