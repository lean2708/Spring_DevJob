package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.GenderEnum;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.entity.relationship.UserSavedJob;

import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_user", indexes = {
        @Index(name = "idx_tbl_user_email", columnList = "email", unique = true),
        @Index(name = "idx_tbl_user_phone", columnList = "phone", unique = true)
})
@SQLRestriction("state = 'ACTIVE'")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User extends BaseEntity {
    @Column(nullable = false)
    String name;
    @Column(unique = true, nullable = false)
    String email;
    @Column(unique = true, nullable = false)
    String phone;
    String password;
    String avatarUrl;

    Integer age;
    @Enumerated(EnumType.STRING)
    GenderEnum gender;
    String address;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "state", nullable = false)
    EntityStatus state;

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


    @PrePersist
    public void prePersist() {
        if (state == null) {
            this.state = EntityStatus.ACTIVE;
        }
    }
}
