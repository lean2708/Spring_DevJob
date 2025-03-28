package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.LevelEnum;
import spring_devjob.entity.relationship.JobHasResume;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.entity.relationship.UserSavedJob;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@SQLRestriction("state = 'ACTIVE'")
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "tbl_job")
public class Job extends BaseEntity {
    @Column(nullable = false)
    String name;
    String location;
    double salary;
    int quantity;
    @Enumerated(EnumType.STRING)
    LevelEnum level;

    @Column(columnDefinition = "MEDIUMTEXT")
    String description;

    LocalDate startDate;
    LocalDate endDate;
    boolean jobStatus = true;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "state", nullable = false)
    EntityStatus state;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    Company company;

    @OneToMany(mappedBy = "job")
    @JsonIgnore
    Set<JobHasResume> resumes = new HashSet<>();

    @OneToMany(mappedBy = "job")
    Set<JobHasSkill> skills = new HashSet<>();

    @OneToMany(mappedBy = "job")
    Set<UserSavedJob> users = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (state == null) {
            this.state = EntityStatus.ACTIVE;
        }
    }
}
