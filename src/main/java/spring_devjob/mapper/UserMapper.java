package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.UserBasic;
import spring_devjob.dto.request.RegisterRequest;
import spring_devjob.dto.request.UserRequest;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRequest request);

    User toUser(RegisterRequest request);

    @Mapping(source = "company", target = "company")
    @Mapping(source = "roles", target = "roles")
    UserResponse toUserResponse(User user);

    UserBasic toUserBasic(User user);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserRequest request);

}
