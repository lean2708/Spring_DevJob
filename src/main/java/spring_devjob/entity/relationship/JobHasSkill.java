package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Job;
import spring_devjob.entity.Skill;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@Entity
@Table(name = "tbl_job_has_skill")
public class JobHasSkill extends RelationBaseEntity{

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    Job job;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    Skill skill;

    public JobHasSkill(Job job, Skill skill) {
        this.job = job;
        this.skill = skill;
    }

}
