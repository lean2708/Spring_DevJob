package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SQLRestriction("state = 'ACTIVE'")
@Table(name = "tbl_role")
public class Role extends BaseEntity {
    String description;

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    Set<UserHasRole> users = new HashSet<>();

    @OneToMany(mappedBy = "role")
    Set<RoleHasPermission> permissions = new HashSet<>();
}
