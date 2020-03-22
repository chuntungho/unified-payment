package com.chuntung.payment.conf;

import jdk.nashorn.internal.objects.annotations.Property;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class PaymentConfiguration {

    @Bean
    @ConfigurationProperties("payment.wxpay")
    public WXPaymentConfig wxPaymentConfig() {
        return new WXPaymentConfig();
    }
}
