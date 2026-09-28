package com.ntd.csdl.repo;

import com.ntd.csdl.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, String> {

    List<Tenant> findByFullNameContainingIgnoreCase(String fullName);

    Tenant findByCccd(String cccd);
}