package spring_devjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.entity.relationship.RoleHasPermission;

import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_permission", indexes = {
        @Index(name = "idx_name", columnList = "name", unique = true)
})
@Entity
@SuperBuilder
@NoArgsConstructor
public class Permission extends BaseEntity {
    @Column(nullable = false, unique = true)
    String name;

    String module;
    String apiPath;
    String method;

    @OneToMany(mappedBy = "permission")
    @JsonIgnore
    Set<RoleHasPermission> roles = new HashSet<>();
}
