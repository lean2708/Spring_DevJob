package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.LevelEnum;
import spring_devjob.service.AuthService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@SQLRestriction("state = 'ACTIVE'")
@NoArgsConstructor
@Table(name = "tbl_job")
public class Job extends BaseEntity {
    String location;
    double salary;
    int quantity;
    @Enumerated(EnumType.STRING)
    LevelEnum level;

    @Column(columnDefinition = "MEDIUMTEXT")
    String description;

    LocalDate startDate;
    LocalDate endDate;
    boolean jobStatus;

    @ManyToOne
    @JoinColumn(name = "company_id")
    Company company;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    @JsonIgnore
    Set<Resume> resumes = new HashSet<>();

    @OneToMany(mappedBy = "job")
    Set<JobHasSkill> skills = new HashSet<>();
}
