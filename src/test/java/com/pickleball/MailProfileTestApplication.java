package com.pickleball;

import com.pickleball.config.MailTlsConfig;
import com.pickleball.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;

/**
 * Profile-aware mail smoke test application.
 *
 * Example:
 * ./gradlew compileTestJava
 * java -cp build/classes/java/test:build/classes/java/main:build/resources/main com.pickleball.MailProfileTestApplication \
 *   --spring.profiles.active=dev --mail.test.to=you@example.com --mail.test.subject="mail test"
 */
public class MailProfileTestApplication {

    private static final Logger log = LoggerFactory.getLogger(MailProfileTestApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(MailSmokeConfig.class)
                .web(WebApplicationType.NONE)
                .run(args);

        int exitCode = 0;
        try {
            Environment env = context.getEnvironment();
            String activeProfiles = String.join(",", env.getActiveProfiles());
            String to = env.getProperty("mail.test.to");
            String subject = env.getProperty("mail.test.subject", "[Pickleball] Mail profile test");
            String body = env.getProperty(
                    "mail.test.body",
                    "Profile mail test at " + LocalDateTime.now()
            );

            if (to == null || to.isBlank()) {
                log.error("Missing recipient. Set --mail.test.to=<email>");
                log.info("Example: --spring.profiles.active=dev --mail.test.to=you@example.com");
                exitCode = 2;
                return;
            }

            boolean mailEnabled = env.getProperty("app.mail.enabled", Boolean.class, true);
            String from = env.getProperty("app.mail.from", "no-reply@lesgo-pickle.local");

            log.info("Active profile(s): {}", activeProfiles.isBlank() ? "(default)" : activeProfiles);
            log.info("Mail enabled: {}", mailEnabled);
            log.info("From: {}", from);
            log.info("To: {}", to);

            MailService mailService = context.getBean(MailService.class);
            mailService.send(to, subject, body);
            log.info("Mail send requested. Check SMTP log/result.");
        } catch (Exception e) {
            log.error("Mail test failed: {}", e.getMessage(), e);
            exitCode = 1;
        } finally {
            int appExitCode = SpringApplication.exit(context);
            System.exit(exitCode != 0 ? exitCode : appExitCode);
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            DataJpaRepositoriesAutoConfiguration.class
    })
    @Import({MailService.class, MailTlsConfig.class})
    static class MailSmokeConfig {
    }
}
