package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.ResumeStateEnum;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SQLRestriction("state = 'ACTIVE'")
@Table(name = "tbl_resume")
public class Resume extends BaseEntity {
    @Column(nullable = false)
    String name;

    String cvUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    ResumeStateEnum resumeStatus = ResumeStateEnum.PENDING;

    boolean primaryCv;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "job_id")
    Job job;

}
