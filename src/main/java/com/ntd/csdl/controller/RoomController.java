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

    @GetMapping
    public ResponseEntity<List<Room>> getAll() {
        return ResponseEntity.ok(roomService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                roomService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Room> create(
            @Valid @RequestBody RoomDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roomService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> update(
            @PathVariable String id,
            @Valid @RequestBody RoomDTO dto) {

        return ResponseEntity.ok(
                roomService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        roomService.delete(id);

        return ResponseEntity.noContent().build();
    }
}