package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.service.AuthService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@SQLRestriction("state = 'ACTIVE'")
@NoArgsConstructor
@Table(name = "tbl_company")
public class Company extends BaseEntity {
    @Column(nullable = false, unique = true)
    String name;
    @Column(columnDefinition = "TEXT")
    String description;
    String address;
    String logoUrl;

    @Column(nullable = false)
    double averageRating = 0.0;

    @Column(nullable = false)
    int totalReviews = 0;


    @OneToMany( mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    Set<Job> jobs = new HashSet<>();

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    Set<Review> reviews = new HashSet<>();


}
