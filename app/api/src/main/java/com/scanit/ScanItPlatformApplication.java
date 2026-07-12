package com.scanit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ScanItPlatformApplication {

	public static void main(String[] args) {
		System.out.println("Working dir: " + System.getProperty("user.dir"));
		SpringApplication.run(ScanItPlatformApplication.class, args);
	}
}
