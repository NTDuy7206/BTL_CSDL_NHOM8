package com.ntd.csdl.config;

import com.ntd.csdl.service.RoomReadinessMigration;
import com.ntd.csdl.service.RoomSamplePhotoService;
import com.ntd.csdl.service.DemoPhoneRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class RoomReadinessInitializer {
    private final RoomReadinessMigration migration;
    private final RoomSamplePhotoService photos;
    private final DemoPhoneRepairService phones;
    @EventListener(ApplicationReadyEvent.class)
    public void completeExistingRooms() { migration.apply(); photos.seed(); phones.repair(); }
}
