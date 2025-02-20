package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.UserBasic;
import spring_devjob.dto.request.UserRequest;
import spring_devjob.dto.response.SkillResponse;
import spring_devjob.dto.response.SubscriberResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.Skill;
import spring_devjob.entity.Subscriber;
import spring_devjob.entity.User;

@Mapper(componentModel = "spring")
public interface SubscriberMapper {
    @Mapping(target = "skills", ignore = true)
    SubscriberResponse toSubscriberResponse(Subscriber subscriber);
}
