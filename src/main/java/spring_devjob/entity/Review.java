package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@SQLRestriction("state = 'ACTIVE'")
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "tbl_review")
public class Review extends BaseEntity{

    @Column(nullable = false)
    double rating;

    @Column(columnDefinition = "TEXT")
    String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    EntityStatus state;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    Company company;

    @PrePersist
    public void prePersist() {
        if (state == null) {
            this.state = EntityStatus.ACTIVE;
        }
    }
}
