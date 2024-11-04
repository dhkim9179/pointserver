package com.example.pointserver.config;

import com.example.pointserver.common.logging.LoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        LoggingFilter loggingFilter = new LoggingFilter();
        loggingFilter.setResponseContentTypePrefixesExcludeLogging("text/css", "application/javascript", "application/octet-stream");
        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>(loggingFilter);
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }
}
