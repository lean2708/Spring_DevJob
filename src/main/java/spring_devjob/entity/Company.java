package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;

import java.util.HashSet;
import java.util.Set;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_company", indexes = {
        @Index(name = "idx_tbl_company_name", columnList = "name", unique = true)
})
@SQLRestriction("state = 'ACTIVE'")
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseEntity {
    @Column(nullable = false, unique = true)
    String name;
    @Column(columnDefinition = "TEXT")
    String description;
    String address;
    String logoUrl;

    @Column(nullable = false)
    Double averageRating;

    @Column(nullable = false)
    Integer totalReviews;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "state", nullable = false)
    EntityStatus state;


    @OneToMany( mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    Set<Job> jobs = new HashSet<>();

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    Set<Review> reviews = new HashSet<>();


    @PrePersist
    public void prePersist() {
        if (state == null) {
            this.state = EntityStatus.ACTIVE;
        }
        if(averageRating == null){
            this.averageRating = 5.0;
        }
        if(totalReviews == null){
            this.totalReviews = 0;
        }
    }
}
