package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.entity.Permission;
import spring_devjob.entity.Role;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "tbl_role_has_permission", indexes = {
        @Index(name = "idx_role_id", columnList = "role_id"),
        @Index(name = "idx_permission_id", columnList = "permission_id")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class RoleHasPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    Permission permission;

    public RoleHasPermission(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }
}
