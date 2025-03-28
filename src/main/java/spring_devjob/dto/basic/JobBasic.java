package spring_devjob.dto.basic;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.ApplicationStatusEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobBasic extends EntityBasic {
    ApplicationStatusEnum applicationStatus;
}
