package dev.aj.data.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = false)
public class Model extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_seq_gen")
    @SequenceGenerator(name = "model_seq_gen", sequenceName = "model_seq", initialValue = 1000, allocationSize = 5)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private Long id;

    private UUID uuid;

    private Date javaUtilDate;

    private java.sql.Date javaSqlDate;

    private LocalDateTime localDateTime;

    private OffsetDateTime offsetDateTime;

    private ZonedDateTime zonedDateTime;

    @Column(name = "java_util_date_tz", columnDefinition = "timestamptz")
    private Date javaUtilDateTZ;

    @Column(name = "java_sql_date_tz", columnDefinition = "timestamptz")
    private java.sql.Date javaSqlDateTZ;

    @Column(name = "local_date_time_tz", columnDefinition = "timestamptz")
    private LocalDateTime localDateTimeTZ;

    @Column(name = "offset_date_time_tz", columnDefinition = "timestamptz")
    private OffsetDateTime offsetDateTimeTZ;

    @Column(name = "zoned_date_time_tz", columnDefinition = "timestamptz")
    private ZonedDateTime zonedDateTimeTZ;
}
