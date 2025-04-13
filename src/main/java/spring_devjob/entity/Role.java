package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.entity.relationship.RoleHasPermission;
import spring_devjob.entity.relationship.UserHasRole;

import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_role", indexes = {
        @Index(name = "idx_tbl_role_name", columnList = "name", unique = true)
})
@Entity
@SuperBuilder
@NoArgsConstructor
public class Role extends BaseEntity {
    @Column(nullable = false, unique = true)
    String name;
    String description;

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    Set<UserHasRole> users = new HashSet<>();

    @OneToMany(mappedBy = "role")
    Set<RoleHasPermission> permissions = new HashSet<>();
}
