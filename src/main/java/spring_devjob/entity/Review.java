package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_review", indexes = {
        @Index(name = "idx_company_id", columnList = "company_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@SQLRestriction("state = 'ACTIVE'")
@Entity
@SuperBuilder
@NoArgsConstructor
public class Review extends BaseEntity{

    @ColumnDefault("5")
    Double rating;

    @Column(columnDefinition = "MEDIUMTEXT")
    String comment;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "state", nullable = false)
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
