package com.ntd.csdl.service;

import com.ntd.csdl.entity.Tenant;
import com.ntd.csdl.repo.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    // CREATE
    public Tenant create(Tenant tenant) {

        if (tenant.getTenantId() == null ||
                tenant.getTenantId().isBlank()) {
            throw new RuntimeException("Tenant ID không được để trống");
        }

        if (tenantRepository.existsById(tenant.getTenantId())) {
            throw new RuntimeException("Tenant đã tồn tại");
        }

        return tenantRepository.save(tenant);
    }

    // READ
    public List<Tenant> getAll() {
        return tenantRepository.findAll();
    }

    public Tenant getById(String id) {
        return tenantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy tenant"));
    }

    // UPDATE
    public Tenant update(String id, Tenant tenant) {

        Tenant existing = getById(id);

        existing.setCccd(tenant.getCccd());
        existing.setFullName(tenant.getFullName());
        existing.setDateOfBirth(tenant.getDateOfBirth());
        existing.setPermanentAddress(tenant.getPermanentAddress());
        existing.setPhoneNumber(tenant.getPhoneNumber());

        return tenantRepository.save(existing);
    }

    // DELETE
    public void delete(String id) {

        if (!tenantRepository.existsById(id)) {
            throw new RuntimeException("Tenant không tồn tại");
        }

        tenantRepository.deleteById(id);
    }

    // SEARCH
    public List<Tenant> searchByName(String name) {
        return tenantRepository
                .findByFullNameContainingIgnoreCase(name);
    }
}