package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.ApplicationStatusEnum;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;

import java.time.LocalDate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_job_has_resume", indexes = {
        @Index(name = "idx_job_has_resume_job_id", columnList = "job_id"),
        @Index(name = "idx_job_has_resume_resume_id", columnList = "resume_id")
})
@SQLRestriction("state = 'ACTIVE'")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class JobHasResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    Resume resume;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    Job job;

    @Enumerated(EnumType.STRING)
    ApplicationStatusEnum applicationStatus;

    LocalDate appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    EntityStatus state;

    @PrePersist
    public void prePersist() {
        if (state == null) {
            this.state = EntityStatus.ACTIVE;
        }
    }

    public JobHasResume(Job job, Resume resume) {
        this.resume = resume;
        this.job = job;
        this.applicationStatus = ApplicationStatusEnum.PENDING;
        this.appliedAt = LocalDate.now();
    }



}
