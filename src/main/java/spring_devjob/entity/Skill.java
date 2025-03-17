package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.entity.relationship.SubHasSkill;

import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SQLRestriction("state = 'ACTIVE'")
@Table(name = "tbl_skill")
public class Skill extends BaseEntity {

    @Column(nullable = false, unique = true)
    String name;

    @OneToMany(mappedBy = "skill")
    @JsonIgnore
    Set<JobHasSkill> jobs = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "skill")
    @JsonIgnore
    Set<SubHasSkill> subscribers = new HashSet<>();
}
