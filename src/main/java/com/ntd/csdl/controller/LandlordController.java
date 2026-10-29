package com.ntd.csdl.controller;
import com.ntd.csdl.service.LandlordService;
import com.ntd.csdl.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.*;
@RestController @RequiredArgsConstructor @RequestMapping("/api/landlords")
public class LandlordController {
 private final LandlordService service;
 public record Setup(Integer roomCount,List<String> existingRoomIds){}
 @GetMapping public List<Map<String,Object>> list(){return service.landlords();}
 @GetMapping("/selection") public Object selection(org.springframework.security.core.Authentication auth){return service.selection(auth.getName());}
 @PutMapping("/selection") public Object select(org.springframework.security.core.Authentication auth,@RequestBody Selection dto){return service.select(auth.getName(),dto.landlordId());}
 record Selection(Long landlordId){}
 @GetMapping("/rooms") public List<Map<String,Object>> rooms(org.springframework.security.core.Authentication auth){return com.ntd.csdl.service.RentalWorkflowService.admin(auth)?service.catalog().stream().filter(r->java.util.Objects.equals(service.owner((String)r.get("roomId")),service.accountId(auth.getName()))).toList():service.selectedCatalog(auth.getName());}
 @GetMapping("/my-rooms") public List<Map<String,Object>> mine(Authentication auth){return service.catalog().stream().filter(r->Objects.equals(service.owner((String)r.get("roomId")),service.accountId(auth.getName()))).toList();}
 @GetMapping("/property") public Map<String,Object> property(Authentication auth){return service.property(auth.getName());}
 @GetMapping("/unassigned") public List<Room> unassigned(){return service.unassigned();}
 @PostMapping("/property") public Map<String,Object> setup(Authentication auth,@RequestBody Setup dto){service.setup(auth.getName(),dto.roomCount(),dto.existingRoomIds());return service.property(auth.getName());}
}
