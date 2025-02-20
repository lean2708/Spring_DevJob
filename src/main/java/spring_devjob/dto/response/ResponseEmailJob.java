package spring_devjob.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseEmailJob {
    String name;
    double salary;
    CompanyEmail company;
    List<SkillEmail> skills;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @Builder
    public static class CompanyEmail{
        String name;
        String logoUrl;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    @AllArgsConstructor
    public static class SkillEmail{
        String name;
    }
}
