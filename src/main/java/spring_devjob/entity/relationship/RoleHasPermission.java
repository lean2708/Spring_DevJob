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
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "tbl_role_has_permission")
public class RoleHasPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

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
