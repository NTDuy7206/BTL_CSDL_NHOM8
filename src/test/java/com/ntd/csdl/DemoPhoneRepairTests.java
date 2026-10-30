package com.ntd.csdl;
import com.ntd.csdl.service.DemoPhoneRepairService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.UUID;

class DemoPhoneRepairTests {
 @Test void repairsOnlyCollidingSampleEmployeesAndPreservesUserContacts() {
  var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("CREATE TABLE demo_seed_history(seed_key VARCHAR(100) PRIMARY KEY,completed BOOLEAN)");
  jdbc.execute("CREATE TABLE employees(employee_id VARCHAR(20) PRIMARY KEY,phone_number VARCHAR(25))");
  jdbc.execute("CREATE TABLE tenants(tenant_id VARCHAR(20) PRIMARY KEY,phone_number VARCHAR(25))");
  jdbc.update("INSERT INTO tenants VALUES('T1','0910000001'),('T2','0908000001'),('T3','0988888888')");
  jdbc.update("INSERT INTO employees VALUES('DEMO_NV01','+84 910 000 001'),('SEED_E01','0910000001'),('DEMO_NV02','0900000002'),('REAL','0988888888'),('DEMO_NV03',NULL)");
  var service=new DemoPhoneRepairService(jdbc);
  assertEquals(2,service.repair());
  assertEquals("0908000002",jdbc.queryForObject("SELECT phone_number FROM employees WHERE employee_id='DEMO_NV01'",String.class));
  assertEquals("0908000003",jdbc.queryForObject("SELECT phone_number FROM employees WHERE employee_id='SEED_E01'",String.class));
  assertEquals("0900000002",jdbc.queryForObject("SELECT phone_number FROM employees WHERE employee_id='DEMO_NV02'",String.class));
  assertEquals("0988888888",jdbc.queryForObject("SELECT phone_number FROM employees WHERE employee_id='REAL'",String.class));
  assertEquals("0910000001",jdbc.queryForObject("SELECT phone_number FROM tenants WHERE tenant_id='T1'",String.class));
  assertNull(jdbc.queryForObject("SELECT phone_number FROM employees WHERE employee_id='DEMO_NV03'",String.class));
  assertEquals(0,service.repair());
 }
}
