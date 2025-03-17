package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SQLRestriction("state = 'ACTIVE'")
@Table(name = "tbl_review")
public class Review extends BaseEntity{

    @Column(nullable = false)
    double rating;

    @Column(columnDefinition = "TEXT")
    String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    Company company;
}
