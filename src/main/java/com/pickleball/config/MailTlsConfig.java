package com.pickleball.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Auto-tune JavaMail properties for implicit TLS (SMTPS / port 465).
 *
 * "Could not convert socket to TLS" is commonly caused by enabling STARTTLS
 * while connecting to an implicit TLS SMTP port (e.g. 465).
 */
@Slf4j
@Configuration
public class MailTlsConfig {

    @Bean
    public BeanPostProcessor javaMailSenderTlsPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (!(bean instanceof JavaMailSenderImpl sender)) {
                    return bean;
                }

                int port = sender.getPort();
                String protocol = sender.getProtocol();
                String host = sender.getHost();

                Properties props = sender.getJavaMailProperties();
                boolean sslEnabled = Boolean.parseBoolean(props.getProperty("mail.smtp.ssl.enable", "false"));
                boolean startTlsEnabled = Boolean.parseBoolean(props.getProperty("mail.smtp.starttls.enable", "false"));

                boolean implicitTls = port == 465 || "smtps".equalsIgnoreCase(protocol) || sslEnabled;
                if (implicitTls) {
                    props.setProperty("mail.smtp.starttls.enable", "false");
                    props.setProperty("mail.smtp.starttls.required", "false");
                    props.setProperty("mail.smtp.ssl.enable", "true");
                    if (host != null && !host.isBlank() && props.getProperty("mail.smtp.ssl.trust") == null) {
                        props.setProperty("mail.smtp.ssl.trust", host);
                    }
                    log.info("Mail TLS tuned for implicit TLS (protocol={}, port={})", protocol, port);
                    return bean;
                }

                // For explicit STARTTLS (typically 587), ensure SSL and STARTTLS are not both enabled.
                if (startTlsEnabled && sslEnabled) {
                    props.setProperty("mail.smtp.ssl.enable", "false");
                    log.info("Mail TLS normalized for STARTTLS (protocol={}, port={}): disabled implicit SSL", protocol, port);
                }

                log.info(
                        "Mail SMTP effective properties (host={}, protocol={}, port={}, starttls.enable={}, starttls.required={}, ssl.enable={}, ssl.trust={})",
                        host,
                        protocol,
                        port,
                        props.getProperty("mail.smtp.starttls.enable", "false"),
                        props.getProperty("mail.smtp.starttls.required", "false"),
                        props.getProperty("mail.smtp.ssl.enable", "false"),
                        props.getProperty("mail.smtp.ssl.trust", "")
                );

                return bean;
            }
        };
    }
}
