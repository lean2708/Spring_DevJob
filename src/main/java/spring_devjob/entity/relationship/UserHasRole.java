package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Role;
import spring_devjob.entity.User;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@SQLRestriction("state = 'ACTIVE'")
@Builder
@NoArgsConstructor
@Entity
@Table(name = "tbl_user_has_role")
public class UserHasRole extends RelationBaseEntity{

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    public UserHasRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

}
