package com.scanit.config;

import com.scanit.ScanItPlatformApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class RenderDatabaseUrlEnvironmentPostProcessorTest {
    private final RenderDatabaseUrlEnvironmentPostProcessor processor =
            new RenderDatabaseUrlEnvironmentPostProcessor();

    @Test
    void shouldTranslateRenderDatabaseUrlForProdProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty(
                "DATABASE_URL",
                "postgresql://scanit%40user:pa%3Ass@db.internal:5432/scanit?sslmode=require"
        );

        processor.postProcessEnvironment(environment, new SpringApplication(ScanItPlatformApplication.class));

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.internal:5432/scanit?sslmode=require");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("scanit@user");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("pa:ss");
    }

    @Test
    void shouldIgnoreRenderDatabaseUrlOutsideProdProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        environment.setProperty("DATABASE_URL", "postgresql://user:password@db.internal:5432/scanit");

        processor.postProcessEnvironment(environment, new SpringApplication(ScanItPlatformApplication.class));

        assertThat(environment.getProperty("spring.datasource.url")).isNull();
        assertThat(environment.getProperty("spring.datasource.username")).isNull();
        assertThat(environment.getProperty("spring.datasource.password")).isNull();
    }
}
