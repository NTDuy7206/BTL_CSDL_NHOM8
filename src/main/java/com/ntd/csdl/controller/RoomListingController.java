package com.ntd.csdl.controller;
import com.ntd.csdl.service.*;import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;import org.springframework.http.*;import org.springframework.web.multipart.MultipartFile;
@RestController @RequiredArgsConstructor @RequestMapping("/api/room-listings")
public class RoomListingController {
 private final RoomListingService service;
 public record Description(String title,String description,String address){}
 @GetMapping public Object list(Authentication auth){return service.list(auth.getName(),RentalWorkflowService.admin(auth));}
 @GetMapping("/{id}") public Object detail(Authentication auth,@PathVariable String id){return service.detail(auth.getName(),RentalWorkflowService.admin(auth),id);}
 @PutMapping("/{id}") public void describe(Authentication auth,@PathVariable String id,@RequestBody Description d){service.describe(auth.getName(),id,d.title(),d.description(),d.address());}
 @PostMapping("/{id}/photos") public Object upload(Authentication auth,@PathVariable String id,@RequestParam("file") MultipartFile file){return java.util.Map.of("id",service.upload(auth.getName(),id,file));}
 @DeleteMapping("/{id}/photos/{photo}") public void remove(Authentication auth,@PathVariable String id,@PathVariable String photo){service.deletePhoto(auth.getName(),id,photo);}
 @GetMapping({"/photos/{id}","/photos/{id}/thumbnail"}) public ResponseEntity<byte[]> photo(Authentication auth,@PathVariable String id,jakarta.servlet.http.HttpServletRequest request){var p=service.photo(auth.getName(),RentalWorkflowService.admin(auth),id,request.getRequestURI().endsWith("/thumbnail"));return ResponseEntity.ok().header("X-Content-Type-Options","nosniff").cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType(p.mime())).body(p.bytes());}
}
