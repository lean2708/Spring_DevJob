package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.entity.relationship.JobHasResume;

import java.util.HashSet;
import java.util.Set;


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

    boolean primaryCv = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "resume")
    Set<JobHasResume> jobs = new HashSet<>();
}
