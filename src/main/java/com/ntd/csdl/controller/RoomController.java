package com.ntd.csdl.controller;

import com.ntd.csdl.dto.RoomDTO;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final com.ntd.csdl.service.LandlordService landlords;

    @GetMapping
    public ResponseEntity<List<Room>> getAll(org.springframework.security.core.Authentication auth) {
        return ResponseEntity.ok(com.ntd.csdl.service.RentalWorkflowService.admin(auth)?landlords.mine(auth.getName()):roomService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(
            @PathVariable String id,org.springframework.security.core.Authentication auth) {
        if(com.ntd.csdl.service.RentalWorkflowService.admin(auth))landlords.requireOwner(auth.getName(),id);
        return ResponseEntity.ok(
                roomService.getById(id)
        );
    }

    private Room blockInventory(){throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT,"Khai báo số phòng tại thiết lập chủ nhà; không thêm phòng khi tạo hồ sơ thuê");}

    @PostMapping
    public ResponseEntity<Room> create(
            @Valid @RequestBody RoomDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(blockInventory());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> update(
            @PathVariable String id,
            @Valid @RequestBody RoomDTO dto, org.springframework.security.core.Authentication auth) {
        landlords.requireOwner(auth.getName(),id);
        return ResponseEntity.ok(
                roomService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT,"Số phòng đã cố định; chỉ được sửa thông tin phòng");


    }
}