package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;
import spring_devjob.service.AuthService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_company", indexes = {
        @Index(name = "idx_name", columnList = "name", unique = true)
})
@SQLRestriction("state = 'ACTIVE'")
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseEntity {
    @Column(nullable = false, unique = true)
    String name;
    @Column(columnDefinition = "MEDIUMTEXT")
    String description;
    String address;
    String logoUrl;

    @ColumnDefault("0")
    @Column(nullable = false)
    Double averageRating;

    @ColumnDefault("0")
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
    }
}
