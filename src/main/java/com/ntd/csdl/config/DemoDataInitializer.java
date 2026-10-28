package com.ntd.csdl.config;
import com.ntd.csdl.service.DemoDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Component @RequiredArgsConstructor
@ConditionalOnProperty(name="app.demo-seed.enabled",havingValue="true",matchIfMissing=true)
public class DemoDataInitializer implements ApplicationRunner {
 private final DemoDataService demo;
 @Override public void run(ApplicationArguments args){demo.seed();}
}
