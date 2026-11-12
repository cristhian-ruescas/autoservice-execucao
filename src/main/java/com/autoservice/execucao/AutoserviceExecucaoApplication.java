package com.autoservice.execucao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AutoserviceExecucaoApplication {

    public static void main(final String[] args) {
        SpringApplication.run(AutoserviceExecucaoApplication.class, args);
    }
}
