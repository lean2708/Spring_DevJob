package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Job;
import spring_devjob.entity.User;

import java.time.LocalDate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_user_saved_job", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_job_id", columnList = "job_id")
})
@SQLRestriction("state = 'ACTIVE'")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class UserSavedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    Job job;

    @Column(nullable = false)
    LocalDate savedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    EntityStatus state;

    @PrePersist
    public void prePersist() {
        if (state == null) {
            this.state = EntityStatus.ACTIVE;
        }
    }

    public UserSavedJob(Job job, User user) {
        this.job = job;
        this.user = user;
        this.savedAt = LocalDate.now();
    }


}
