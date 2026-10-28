package com.ntd.csdl.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import java.util.*;

@Service @RequiredArgsConstructor
public class DemoDataService {
 private final JdbcTemplate jdbc;
 @Transactional public void seed() {
   jdbc.update("INSERT INTO demo_seed_history(seed_key,completed) VALUES ('coursework-v1',false) ON DUPLICATE KEY UPDATE seed_key=seed_key");
   Boolean done=jdbc.queryForObject("SELECT completed FROM demo_seed_history WHERE seed_key='coursework-v1' FOR UPDATE",Boolean.class);
   if(Boolean.TRUE.equals(done))return;
   ResourceDatabasePopulator script=new ResourceDatabasePopulator(new ClassPathResource("demo-data.sql"));
   script.setSqlScriptEncoding("UTF-8");script.execute(Objects.requireNonNull(jdbc.getDataSource()));
   jdbc.update("UPDATE demo_seed_history SET completed=true WHERE seed_key='coursework-v1'");
 }
}
