package com.ntd.csdl.config;

import com.ntd.csdl.service.RoomReadinessMigration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class RoomReadinessInitializer {
    private final RoomReadinessMigration migration;
    @EventListener(ApplicationReadyEvent.class)
    public void completeExistingRooms() { migration.apply(); }
}
