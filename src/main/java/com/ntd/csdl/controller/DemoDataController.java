package com.ntd.csdl.controller;
import com.ntd.csdl.service.DemoDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequiredArgsConstructor @RequestMapping("/api/demo")
public class DemoDataController {
 private final DemoDataService demo;
 @PostMapping("/my-rental") public Map<String,Object> myRental(Authentication auth){return demo.myRental(auth.getName());}
}
