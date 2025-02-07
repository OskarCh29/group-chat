package pl.chat.groupchat.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.chat.groupchat.filters.SecurityFilter;
import pl.chat.groupchat.services.AuthorizationService;

@Configuration
@Getter
public class SecurityConfig {

    @Value("${security.saltPrefix}")
    private String saltPrefix;

    @Value("${security.saltSuffix}")
    private String saltSuffix;

    @Bean
    public SecurityFilter securityFilter(AuthorizationService authorizationService, ObjectMapper mapper) {
        return new SecurityFilter(authorizationService, mapper);
    }

    @Bean
    public FilterRegistrationBean<SecurityFilter> securityFilterRegistration(SecurityFilter securityFilter) {
        FilterRegistrationBean<SecurityFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(securityFilter);
        registrationBean.addUrlPatterns("/chat/send-message");
        return registrationBean;
    }
}

