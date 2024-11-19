package pl.chat.groupchat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.chat.groupchat.utils.DateTimeDeserializer;
import pl.chat.groupchat.utils.DateTimeSerializer;

import java.time.LocalDateTime;

@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        module.addSerializer(LocalDateTime.class,new DateTimeSerializer());
        module.addDeserializer(LocalDateTime.class,new DateTimeDeserializer());
        objectMapper.registerModule(module);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
