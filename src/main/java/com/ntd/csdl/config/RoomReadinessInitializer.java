package com.ntd.csdl.config;

import com.ntd.csdl.service.RoomReadinessMigration;
import com.ntd.csdl.service.DemoPhoneRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class RoomReadinessInitializer {
    private final RoomReadinessMigration migration;
    private final DemoPhoneRepairService phones;
    private final com.ntd.csdl.service.TenantOwnershipSchemaRepair ownership;
    @EventListener(ApplicationReadyEvent.class)
    public void completeExistingRooms() {
        ownership.apply();
        migration.apply();
        migration.applyDemoRates();
        int repriced = migration.applyListedRates();
        if (repriced > 0) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.INFO, "Đã cập nhật giá niêm yết của {0} phòng", repriced);
        }
        phones.repair();
    }
}
