package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "tbl_subscriber")
public class Subscriber extends BaseEntity {
    String email;

    LocalDate startDate;
    LocalDate expiryDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "subscribers" })
    @JoinTable(name = "subscriber_skill", joinColumns = @JoinColumn(name =
            "subscriber_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    List<Skill> skills;
}
