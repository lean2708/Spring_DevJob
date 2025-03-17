package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.entity.relationship.SubHasSkill;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@SQLRestriction("state = 'ACTIVE'")
@NoArgsConstructor
@Table(name = "tbl_subscriber")
public class Subscriber extends BaseEntity {
    @Column(nullable = false)
    String name;
    @Column(nullable = false, unique = true)
    String email;

    LocalDate startDate;
    LocalDate expiryDate;

    @OneToMany(mappedBy = "subscriber")
    Set<SubHasSkill> skills = new HashSet<>();
}
