package spring_devjob.entity.history;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "tbl_user_history")
public class UserHistory extends BaseHistoryEntity{

    @Column(unique = true, nullable = false)
    String email;
    @Column(unique = true, nullable = false)
    String phone;

}
