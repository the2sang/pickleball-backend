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

                Properties props = sender.getJavaMailProperties();
                boolean sslEnabled = Boolean.parseBoolean(props.getProperty("mail.smtp.ssl.enable", "false"));

                boolean implicitTls = port == 465 || "smtps".equalsIgnoreCase(protocol) || sslEnabled;
                if (!implicitTls) {
                    return bean;
                }

                props.setProperty("mail.smtp.starttls.enable", "false");
                props.setProperty("mail.smtp.starttls.required", "false");
                props.setProperty("mail.smtp.ssl.enable", "true");

                log.info("Mail TLS tuned for implicit TLS (protocol={}, port={})", protocol, port);
                return bean;
            }
        };
    }
}
