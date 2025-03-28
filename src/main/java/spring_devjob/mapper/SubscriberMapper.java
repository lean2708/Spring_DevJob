package spring_devjob.mapper;

import org.mapstruct.*;
import org.springframework.context.annotation.DependsOn;
import spring_devjob.dto.response.SubscriberResponse;
import spring_devjob.entity.*;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {SkillMapper.class})
public interface SubscriberMapper {

    @Mapping(target = "skills", source = "skills", qualifiedByName = "subHasSkillToEntityBasic")
    SubscriberResponse toSubscriberResponse(Subscriber subscriber);

}
