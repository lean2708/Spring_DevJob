package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.GenderEnum;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.entity.relationship.UserSavedJob;

import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SQLRestriction("state = 'ACTIVE'")
@Table(name = "tbl_user")
public class User extends BaseEntity {
    @Column(nullable = false)
    String name;
    @Column(unique = true, nullable = false)
    String email;
    @Column(unique = true, nullable = false)
    String phone;
    String password;
    String avatarUrl;
    int age;
    @Enumerated(EnumType.STRING)
    GenderEnum gender;
    String address;

    @ManyToOne
    @JoinColumn(name = "company_id")
    Company company;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    Set<Resume> resumes = new HashSet<>();

    @OneToMany(mappedBy = "user")
    Set<UserSavedJob> jobs = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    Set<UserHasRole> roles = new HashSet<>();


}
