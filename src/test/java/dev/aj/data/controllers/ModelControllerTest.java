package dev.aj.data.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import dev.aj.data.domain.model.Model;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"server.port=9595",
        "spring.jpa.hibernate.ddl-auto=none",
                                    "spring.jpa.properties.hibernate.jdbc.time_zone=UTC"
})
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag(value = "integration")
@Slf4j
class ModelControllerTest {

    @Container
//    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("microservices-db")
            .withUsername("aj")
            .withPassword("very-secret")
            .withAccessToHost(true)
            .withInitScript("data.sql")
            .withExposedPorts(5432)
            .withReuse(true)
            .withCreateContainerCmdModifier(cmd -> {
                cmd.withHostConfig(new HostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(8765), new ExposedPort(5432))));
            });

    @DynamicPropertySource
    public static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",  () -> postgresContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () ->  postgresContainer.getUsername());
        registry.add("spring.datasource.password", () ->  postgresContainer.getPassword());
    }

    private static RestClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    private Model model;

    private List<Model> modelsFromJson;

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
    }

    @SneakyThrows
    @Test
    @Order(value = 1)
    void persistCurrentModel() {

        ResponseEntity<Model> responseEntity = restClient.post().uri("/timezone/current")
                                                         .retrieve()
                                                         .toEntity(Model.class);

        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        Model responseBody = responseEntity.getBody();

        log.info("Response object received at the 'front-end':%n%s".formatted(
                objectMapper.writeValueAsString(responseBody)));

        Assertions.assertAll("Asserting new Model",
                             () -> Assertions.assertEquals(HttpStatusCode.valueOf(200), responseStatus),
                             () -> Assertions.assertNotNull(responseBody.getId()));
    }

    @SneakyThrows
    @Test
    @Order(value = 2)
    void persistGivenModel() {

        InputStream resourceAsStream = this.getClass().getResourceAsStream("/model.json");
        modelsFromJson = objectMapper.readValue(resourceAsStream, new TypeReference<List<Model>>() {
        });

        ResponseEntity<List<Model>> responseEntity = restClient.post().uri("/timezone/given/list")
                                                               .body(modelsFromJson)
                                                               .retrieve()
                                                               .toEntity(new ParameterizedTypeReference<List<Model>>() {
                                                               });

        HttpStatusCode statusCode = responseEntity.getStatusCode();
        List<Model> savedModel = responseEntity.getBody();

        objectMapper.writeValueAsString(savedModel);

        Assertions.assertAll("Asserting saved model",
                             () -> Assertions.assertTrue(statusCode.is2xxSuccessful()),
                             () -> Assertions.assertNotNull(savedModel),
                             () -> Assertions.assertNotNull(savedModel.getFirst()),
                             () -> Assertions.assertEquals(modelsFromJson.getFirst().getJavaUtilDate().getTime(),
                                                           savedModel.getFirst().getJavaUtilDate().getTime()));
    }

    @SneakyThrows
    @Test
    void persistDateAtBSTAt2300AndFetchWithJVMAtBST() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        ZonedDateTime feb4th2024AtMidnight = ZonedDateTime.ofInstant(
                LocalDateTime.of(2024, Month.JULY, 4, 0, 0, 0, 0),
                ZoneOffset.UTC,
                ZoneId.of("UTC"));

        ZonedDateTime feb3rd2024At2300 = feb4th2024AtMidnight.minusHours(1);

        Model model = Model.builder()
                           .javaUtilDate(Date.from(feb3rd2024At2300.toInstant()))
                           .javaUtilDateTZ(Date.from(feb3rd2024At2300.toInstant()))
                           .javaSqlDate(new java.sql.Date(System.currentTimeMillis()))
                           .javaSqlDateTZ(new java.sql.Date(System.currentTimeMillis()))
                           .localDateTime(LocalDateTime.now())
                           .localDateTimeTZ(LocalDateTime.now())
                           .offsetDateTime(OffsetDateTime.now())
                           .offsetDateTimeTZ(OffsetDateTime.now())
                           .zonedDateTime(ZonedDateTime.now())
                           .zonedDateTimeTZ(ZonedDateTime.now())
                           .uuid(UUID.randomUUID())
                           .build();

        ResponseEntity<Model> responseEntity = restClient.post().uri("/timezone/given")
                                                         .body(model)
                                                         .retrieve()
                                                         .toEntity(Model.class);
        Model savedModel = responseEntity.getBody();

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        ResponseEntity<Model> responseEntity1 = restClient.get().uri("timezone/{id}", savedModel.getId())
                                                          .retrieve()
                                                          .toEntity(Model.class);
        Model savedModel1 = responseEntity1.getBody();

        Assertions.assertAll("Asserting saved model",
                             () -> Assertions.assertEquals(savedModel.getJavaUtilDateTZ(),
                                                           savedModel1.getJavaUtilDateTZ()),
                             () -> Assertions.assertEquals(model.getJavaUtilDateTZ(),
                                                           savedModel1.getJavaUtilDateTZ()),
                             () -> Assertions.assertEquals(savedModel.getJavaUtilDate(),
                                                           savedModel1.getJavaUtilDate()),
                             () -> Assertions.assertEquals(model.getJavaUtilDate(),
                                                           savedModel1.getJavaUtilDate())
        );
    }

    @SneakyThrows
    @Test
    void persistDateAtUTCAt2300AndFetchWithJVMAtBST() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        ZonedDateTime feb4th2024AtMidnight = ZonedDateTime.ofInstant(
                LocalDateTime.of(2024, Month.JULY, 4, 0, 0, 0, 0),
                ZoneOffset.UTC,
                ZoneId.of("UTC"));

        ZonedDateTime feb3rd2024At2300 = feb4th2024AtMidnight.minusHours(1);

        Model model = Model.builder()
                           .javaUtilDate(Date.from(feb3rd2024At2300.toInstant()))
                           .javaUtilDateTZ(Date.from(feb3rd2024At2300.toInstant()))
                           .javaSqlDate(new java.sql.Date(System.currentTimeMillis()))
                           .javaSqlDateTZ(new java.sql.Date(System.currentTimeMillis()))
                           .localDateTime(LocalDateTime.now())
                           .localDateTimeTZ(LocalDateTime.now())
                           .offsetDateTime(OffsetDateTime.now())
                           .offsetDateTimeTZ(OffsetDateTime.now())
                           .zonedDateTime(ZonedDateTime.now())
                           .zonedDateTimeTZ(ZonedDateTime.now())
                           .uuid(UUID.randomUUID())
                           .build();

        ResponseEntity<Model> responseEntity = restClient.post().uri("/timezone/given")
                                                         .body(model)
                                                         .retrieve()
                                                         .toEntity(Model.class);
        Model savedModel = responseEntity.getBody();

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        ResponseEntity<Model> responseEntity1 = restClient.get().uri("timezone/{id}", savedModel.getId())
                                                          .retrieve()
                                                          .toEntity(Model.class);
        Model savedModel1 = responseEntity1.getBody();

        Assertions.assertAll("Asserting saved model",
                             () -> Assertions.assertEquals(savedModel.getJavaUtilDateTZ(), savedModel1.getJavaUtilDateTZ()),
                             () -> Assertions.assertEquals(model.getJavaUtilDateTZ(), savedModel1.getJavaUtilDateTZ()),
                             () -> Assertions.assertEquals(savedModel.getJavaUtilDate(), savedModel1.getJavaUtilDate()),
                             () -> Assertions.assertEquals(model.getJavaUtilDate(), savedModel1.getJavaUtilDate())
        );
    }

    @SneakyThrows
    @Test
    void persistDateAtCurrentUTCWithJVMAtAuSydney() {
        TimeZone.setDefault(TimeZone.getTimeZone("Australia/Sydney"));

        ZonedDateTime feb4th2024AtMidnight = ZonedDateTime.ofInstant(
                LocalDateTime.of(2024, Month.JULY, 4, 0, 0, 0, 0),
                ZoneOffset.UTC,
                ZoneId.of("UTC"));

        ZonedDateTime feb3rd2024At2300 = feb4th2024AtMidnight.minusHours(1);

        OffsetDateTime nowAtUTC = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));

        Model model = Model.builder()
                           .javaUtilDate(Date.from(nowAtUTC.toInstant()))
                           .javaUtilDateTZ(Date.from(nowAtUTC.toInstant()))
                           .javaSqlDate(new java.sql.Date(System.currentTimeMillis()))
                           .javaSqlDateTZ(new java.sql.Date(System.currentTimeMillis()))
                           .localDateTime(LocalDateTime.now())
                           .localDateTimeTZ(LocalDateTime.now())
                           .offsetDateTime(nowAtUTC)
                           .offsetDateTimeTZ(nowAtUTC)
                           .zonedDateTime(ZonedDateTime.now())
                           .zonedDateTimeTZ(ZonedDateTime.now())
                           .uuid(UUID.randomUUID())
                           .build();

        ResponseEntity<Model> responseEntity = restClient.post().uri("/timezone/given")
                                                         .body(model)
                                                         .retrieve()
                                                         .toEntity(Model.class);
        Model savedModel = responseEntity.getBody();

//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        ResponseEntity<Model> responseEntity1 = restClient.get().uri("timezone/{id}", savedModel.getId())
                                                          .retrieve()
                                                          .toEntity(Model.class);
        Model savedModel1 = responseEntity1.getBody();

        Assertions.assertAll("Asserting saved model",
                             () -> Assertions.assertEquals(savedModel.getJavaUtilDateTZ(), savedModel1.getJavaUtilDateTZ()),
                             () -> Assertions.assertEquals(savedModel.getJavaUtilDate(), savedModel1.getJavaUtilDate()),
                             () -> Assertions.assertEquals(savedModel.getOffsetDateTime(), savedModel1.getOffsetDateTime()),
//                             () -> org.assertj.core.api.Assertions.assertThat(savedModel.getOffsetDateTime())
//                                                                  .isEqualToIgnoringNanos(savedModel1.getOffsetDateTime()),
                             () -> Assertions.assertEquals(savedModel.getOffsetDateTimeTZ(), savedModel1.getOffsetDateTimeTZ())
//                             () -> org.assertj.core.api.Assertions.assertThat(savedModel.getOffsetDateTimeTZ())
//                                                                  .isEqualToIgnoringNanos(savedModel1.getOffsetDateTimeTZ())
        );
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
                             () -> Assertions.assertNotNull(savedModel),
                             () -> Assertions.assertNotNull(savedModel.getId()));
    }

    @Test
    @Order(value = 4)
    void getAllModels() {

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
        ResponseEntity<List<Model>> responseEntity = restClient.get().uri("/timezone/all")
                                                               .retrieve()
                                                               .toEntity(new ParameterizedTypeReference<List<Model>>() {
                                                               });

        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        List<Model> body = responseEntity.getBody();

        Assertions.assertAll("Asserting saved model",
                             () -> Assertions.assertTrue(responseStatus.is2xxSuccessful()),
                             () -> Assertions.assertNotNull(body),
                             () -> Assertions.assertEquals(6, body.size()));
    }
}