package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Job;
import spring_devjob.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_user_saved_job")
public class UserSavedJob extends RelationBaseEntity{

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    Job job;

    @Column(nullable = false)
    LocalDate savedAt;
}
