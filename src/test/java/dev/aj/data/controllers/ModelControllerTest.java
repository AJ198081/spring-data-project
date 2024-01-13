package dev.aj.data.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import dev.aj.data.domain.model.Model;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"server.port=9595", "spring.jpa.hibernate.ddl-auto=none"})
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag(value = "integration")
class ModelControllerTest {

    @Container
    @ServiceConnection
    public static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("microservices-db")
            .withUsername("aj")
            .withPassword("very-secret")
            .withAccessToHost(true)
            .withInitScript("data.sql")
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd -> {
                cmd.withHostConfig(new HostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(7654), new ExposedPort(5432))));
            });

    private static RestClient restClient;
    @Autowired
    private Environment environment;
    @Autowired
    private ObjectMapper objectMapper;
    private Model model;

    private Model modelFromJson;

    private String jdbcUrl; //jdbc:postgresql://localhost:7654/microservices-db

    @BeforeAll
    static void beforeAll() {
        restClient = RestClient.builder()
                               .baseUrl("http://localhost:%d".formatted(9595))
                               .build();
    }

    @SneakyThrows
    @BeforeEach
    void setUp() {
        model = Model.builder()
                     .uuid(UUID.randomUUID())
                     .localDateTime(LocalDateTime.now())
                     .offsetDateTime(OffsetDateTime.now())
                     .zonedDateTime(ZonedDateTime.now())
                     .javaUtilDate(new Date())
                     .javaUtilDateTZ(new Date())
                     .javaSqlDate(new java.sql.Date(System.currentTimeMillis()))
                     .javaSqlDateTZ(new java.sql.Date(System.currentTimeMillis()))
                     .build();
        jdbcUrl = postgresContainer.getJdbcUrl();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/model.json");
        modelFromJson = objectMapper.readValue(resourceAsStream, Model.class);
    }

    @Test
    @Order(value = 1)
    void persistCurrentModel() {
        ResponseEntity<Model> responseEntity = restClient.post().uri("/timezone/current")
                                                         .retrieve()
                                                         .toEntity(Model.class);

        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        Model responseBody = responseEntity.getBody();

        Assertions.assertAll("Asserting new Model",
                             () -> Assertions.assertEquals(HttpStatusCode.valueOf(200), responseStatus),
                             () -> Assertions.assertNotNull(responseBody.getId()));
    }

    @Test
    @Order(value = 2)
    void persistGivenModel() {
        ResponseEntity<Model> responseEntity = restClient.post().uri("/timezone/given")
                                                         .body(modelFromJson)
                                                         .retrieve()
                                                         .toEntity(Model.class);

        HttpStatusCode statusCode = responseEntity.getStatusCode();
        Model savedModel = responseEntity.getBody();

        Assertions.assertAll("Asserting saved model",
                             () -> Assertions.assertTrue(statusCode.is2xxSuccessful()),
                             () -> Assertions.assertNotNull(savedModel.getId()),
                             () -> Assertions.assertEquals(modelFromJson.getJavaUtilDate().getTime(),
                                                           savedModel.getJavaUtilDate().getTime()));
    }

    @Test
    @Order(value = 3)
    void persistAnotherGivenModel() {
        ResponseEntity<Model> responseEntity = restClient.post().uri("/timezone/given")
                                                         .body(model)
                                                         .retrieve()
                                                         .toEntity(Model.class);

        HttpStatusCode statusCode = responseEntity.getStatusCode();
        Model savedModel = responseEntity.getBody();

        Assertions.assertAll("Asserting saved model",
                             () -> Assertions.assertTrue(statusCode.is2xxSuccessful()),
                             () -> Assertions.assertNotNull(savedModel.getId()));
    }

    @Test
    @Order(value = 3)
    void getAllModels() {
        ResponseEntity<List<Model>> responseEntity = restClient.get().uri("/timezone/all")
                                                               .retrieve()
                                                               .toEntity(new ParameterizedTypeReference<List<Model>>() {
                                                               });

        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        List<Model> body = responseEntity.getBody();

        Assertions.assertAll("Asserting saved model",
                             () -> Assertions.assertTrue(responseStatus.is2xxSuccessful()),
                             () -> Assertions.assertTrue(body.size() == 4));
    }

    @Test
    void getAModel() {
    }

    @Test
    void updateAnExistingModel() {
    }
}