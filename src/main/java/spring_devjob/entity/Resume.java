package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.relationship.JobHasResume;

import java.util.HashSet;
import java.util.Set;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_resume", indexes = {
        @Index(name = "idx_tbl_resume_user_id", columnList = "user_id")
})
@SQLRestriction("state = 'ACTIVE'")
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Resume extends BaseEntity {
    @Column(nullable = false)
    String name;

    String cvUrl;

    Boolean primaryCv;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "state", nullable = false)
    EntityStatus state;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "resume", fetch = FetchType.EAGER)
    Set<JobHasResume> jobs = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (state == null) {
            this.state = EntityStatus.ACTIVE;
        }
        if(primaryCv == null){
            this.primaryCv = true;
        }
    }
}
