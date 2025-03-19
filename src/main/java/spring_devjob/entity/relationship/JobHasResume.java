package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.ApplicationStatusEnum;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;

import java.time.LocalDate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@SQLRestriction("state = 'ACTIVE'")
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "tbl_job_has_resume")
public class JobHasResume extends RelationBaseEntity {

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    Resume resume;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    Job job;

    @Enumerated(EnumType.STRING)
    ApplicationStatusEnum applicationStatus;

    LocalDate appliedAt;

    public JobHasResume( Job job, Resume resume) {
        this.resume = resume;
        this.job = job;
        this.applicationStatus = ApplicationStatusEnum.PENDING;
        this.appliedAt = LocalDate.now();
    }
}
