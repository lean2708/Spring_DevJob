package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Skill;
import spring_devjob.entity.Subscriber;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@Entity
@Table(name = "tbl_subscriber_has_skill")
public class SubHasSkill extends RelationBaseEntity{

    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    Subscriber subscriber;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    Skill skill;

    public SubHasSkill(Subscriber subscriber, Skill skill) {
        this.subscriber = subscriber;
        this.skill = skill;
    }
}
