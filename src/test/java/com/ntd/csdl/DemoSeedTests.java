package com.ntd.csdl;
import com.ntd.csdl.service.DemoDataService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class DemoSeedTests {
 @Autowired DemoDataService demo;
 @Autowired JdbcTemplate jdbc;
 @Test void runningSeedAgainDoesNotDuplicateOrOverwriteEditedRows(){
   demo.seed();
   Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM contracts",Integer.class);
   String name=jdbc.queryForObject("SELECT full_name FROM employees WHERE employee_id='DEMO_NV01'",String.class);
   try {
     jdbc.update("UPDATE employees SET full_name='Edited by test' WHERE employee_id='DEMO_NV01'");
     demo.seed();
     assertEquals(count,jdbc.queryForObject("SELECT COUNT(*) FROM contracts",Integer.class));
     assertEquals("Edited by test",jdbc.queryForObject("SELECT full_name FROM employees WHERE employee_id='DEMO_NV01'",String.class));
   }finally{jdbc.update("UPDATE employees SET full_name=? WHERE employee_id='DEMO_NV01'",name);}
 }
}
