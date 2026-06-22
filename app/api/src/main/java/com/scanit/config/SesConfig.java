package com.scanit.config;

import io.awspring.cloud.ses.SimpleEmailServiceMailSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SesConfig {
    @Bean
    public SesClient sesClient() {
        return SesClient.builder().region(Region.US_EAST_1).build();
    }
    
    @Bean
    public SimpleEmailServiceMailSender emailSender(SesClient sesClient) {
        return new SimpleEmailServiceMailSender(sesClient);
    }
}
