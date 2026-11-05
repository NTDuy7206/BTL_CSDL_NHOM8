package com.ntd.csdl.service;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** A tenant's owner is an account; registering room inventory is a separate step. */
@Service @RequiredArgsConstructor
public class TenantOwnershipSchemaRepair {
 private final JdbcTemplate jdbc;
 public void apply() {
  var keys=jdbc.queryForList("SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='tenant_landlords' AND COLUMN_NAME='user_id' AND REFERENCED_TABLE_NAME='landlord_properties'");
  for(var key:keys){
   String name=key.get("CONSTRAINT_NAME").toString().replace("`","``");
   jdbc.execute("ALTER TABLE tenant_landlords DROP FOREIGN KEY `"+name+"`, ADD CONSTRAINT fk_tenant_owner_account FOREIGN KEY(user_id) REFERENCES user(id)");
  }
 }
}
