package pl.chat.groupchat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.chat.groupchat.utils.DateTimeDeserializer;
import pl.chat.groupchat.utils.DateTimeSerializer;

import java.time.LocalDateTime;

@Configuration
@Getter
public class AppConfig {
    @Value("${security.saltPrefix}")
    private String saltPrefix;

    @Value("${security.saltSuffix}")
    private String saltSuffix;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        module.addSerializer(LocalDateTime.class, new DateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new DateTimeDeserializer());
        objectMapper.registerModule(module);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

}
