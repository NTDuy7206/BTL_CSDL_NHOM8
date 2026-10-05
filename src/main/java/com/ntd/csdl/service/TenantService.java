package com.ntd.csdl.service;

import com.ntd.csdl.dto.TenantDTO;
import com.ntd.csdl.entity.Tenant;
import com.ntd.csdl.repo.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    // CREATE
    @CacheEvict(
            value = {
                    "tenants",
                    "tenantById",
                    "tenantsByName"
            },
            allEntries = true
    )
    public Tenant create(TenantDTO dto) {

        if (dto.getTenantId() == null ||
                dto.getTenantId().isBlank()) {

            throw new RuntimeException(
                    "Tenant ID không được để trống"
            );
        }

        if (tenantRepository.existsById(dto.getTenantId())) {
            throw new RuntimeException(
                    "Tenant đã tồn tại"
            );
        }

        Tenant tenant = new Tenant();

        tenant.setTenantId(dto.getTenantId());
        tenant.setCccd(dto.getCccd());
        tenant.setFullName(dto.getFullName());
        tenant.setDateOfBirth(dto.getDateOfBirth());
        tenant.setPermanentAddress(dto.getPermanentAddress());
        tenant.setPhoneNumber(dto.getPhoneNumber());

        return tenantRepository.save(tenant);
    }

    // GET ALL
    @Cacheable(value = "tenants")
    public List<Tenant> getAll() {
        return tenantRepository.findAll();
    }

    // GET BY ID
    @Cacheable(value = "tenantById", key = "#id")
    public Tenant getById(String id) {

        return tenantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy tenant"
                        )
                );
    }

    // UPDATE
    @CacheEvict(
            value = {
                    "tenants",
                    "tenantById",
                    "tenantsByName"
            },
            allEntries = true
    )
    public Tenant update(String id, TenantDTO dto) {

        Tenant existing = tenantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy tenant"
                        )
                );

        existing.setCccd(dto.getCccd());
        existing.setFullName(dto.getFullName());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setPermanentAddress(dto.getPermanentAddress());
        existing.setPhoneNumber(dto.getPhoneNumber());

        return tenantRepository.save(existing);
    }

    // DELETE
    @CacheEvict(
            value = {
                    "tenants",
                    "tenantById",
                    "tenantsByName"
            },
            allEntries = true
    )
    public void delete(String id) {

        if (!tenantRepository.existsById(id)) {
            throw new RuntimeException(
                    "Tenant không tồn tại"
            );
        }

        tenantRepository.deleteById(id);
    }

    // SEARCH
    @Cacheable(value = "tenantsByName", key = "#name")
    public List<Tenant> searchByName(String name) {

        return tenantRepository
                .findByFullNameContainingIgnoreCase(name);
    }
}