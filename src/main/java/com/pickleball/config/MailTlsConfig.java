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
    public static BeanPostProcessor javaMailSenderTlsPostProcessor() {
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
                boolean explicitStartTls = port == 587 || startTlsEnabled;

                if (implicitTls) {
                    props.setProperty("mail.smtp.starttls.enable", "false");
                    props.setProperty("mail.smtp.starttls.required", "false");
                    props.setProperty("mail.smtp.ssl.enable", "true");
                } else if (explicitStartTls) {
                    // Standard STARTTLS mode (e.g. smtp.gmail.com:587)
                    props.setProperty("mail.smtp.ssl.enable", "false");
                    props.setProperty("mail.smtp.starttls.enable", "true");
                }

                // For Gmail, trust the SMTP host explicitly when trust is not set.
                String trust = props.getProperty("mail.smtp.ssl.trust", "");
                if ((trust == null || trust.isBlank()) && host != null && host.contains("gmail.com")) {
                    props.setProperty("mail.smtp.ssl.trust", "smtp.gmail.com");
                }

                log.info(
                        "Mail TLS normalized host={} protocol={} port={} starttls={} ssl={} trust={}",
                        host,
                        protocol,
                        port,
                        props.getProperty("mail.smtp.starttls.enable"),
                        props.getProperty("mail.smtp.ssl.enable"),
                        props.getProperty("mail.smtp.ssl.trust")
                );
                return bean;
            }
        };
    }
}
