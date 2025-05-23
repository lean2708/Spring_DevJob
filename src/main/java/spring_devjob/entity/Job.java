package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
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
@Table(name = "tbl_job", indexes = {
        @Index(name = "idx_tbl_job_company_id", columnList = "company_id"),
        @Index(name = "idx_tbl_job_name", columnList = "name")
})
@SQLRestriction("state = 'ACTIVE'")
@SuperBuilder
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Job extends BaseEntity {
    @Column(nullable = false)
    String name;
    String location;
    Double salary;
    Integer quantity;
    @Enumerated(EnumType.STRING)
    LevelEnum level;

    @Column(columnDefinition = "TEXT")
    String description;

    LocalDate startDate;
    LocalDate endDate;
    Boolean jobStatus;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "state", nullable = false)
    EntityStatus state;

    @CreatedBy
    String createdBy;
    @LastModifiedBy
    String updatedBy;
    @CreationTimestamp
    LocalDate createdAt;
    @UpdateTimestamp
    LocalDate updatedAt;

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
        if(jobStatus == null){
            this.jobStatus = true;
        }
    }
}
