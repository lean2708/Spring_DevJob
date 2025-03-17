package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Permission;
import spring_devjob.entity.Role;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@Entity
@Table(name = "tbl_role_has_permission")
public class RoleHasPermission extends RelationBaseEntity {

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
