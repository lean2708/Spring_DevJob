package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.entity.Skill;
import spring_devjob.entity.Subscriber;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_subscriber_has_skill", indexes = {
        @Index(name = "idx_subscriber_has_skill_subscriber_id", columnList = "subscriber_id"),
        @Index(name = "idx_subscriber_has_skill_skill_id", columnList = "skill_id")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class SubHasSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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
