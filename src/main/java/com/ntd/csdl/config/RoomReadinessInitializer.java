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
    private final com.ntd.csdl.service.TenantOwnershipSchemaRepair ownership;
    @EventListener(ApplicationReadyEvent.class)
    public void completeExistingRooms() { ownership.apply(); migration.apply(); photos.seed(); phones.repair(); }
}
