package dev.aj.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableJpaAuditing
public class SpringDataProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringDataProjectApplication.class, args);
    }

    @Bean(value = "restClient")
    public RestClient getRestClient() {
        return RestClient.builder().
                         baseUrl("http://localhost:9595")
                         .build();
    }

}
