package spring_devjob.entity.history;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.LevelEnum;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "tbl_job_history")
public class JobHistory extends BaseHistoryEntity{

    String location;
    double salary;
    int quantity;
    @Enumerated(EnumType.STRING)
    LevelEnum level;
}
