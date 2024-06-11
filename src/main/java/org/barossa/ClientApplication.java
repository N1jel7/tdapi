package org.barossa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(excludeName = {"dev.voroby.springframework.telegram.TelegramClientAutoConfiguration"})
@ConfigurationPropertiesScan(basePackages = "dev.voroby.springframework.telegram.properties")
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}