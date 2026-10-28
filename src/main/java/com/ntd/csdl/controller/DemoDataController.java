package com.ntd.csdl.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/demo")
public class DemoDataController {
 @PostMapping("/my-rental") public void myRental(){throw new ResponseStatusException(HttpStatus.GONE,"Chức năng tạo hồ sơ mẫu đã bỏ. Hãy chọn chủ nhà và phòng có sẵn trong Đăng ký hợp đồng");}
}
