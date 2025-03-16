package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.basic.UserBasic;
import spring_devjob.dto.request.RegisterRequest;
import spring_devjob.dto.request.UserRequest;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.User;
import spring_devjob.entity.UserHasRole;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRequest request);

    User toUser(RegisterRequest request);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "userHasRoleToEntityBasic")
    UserResponse toUserResponse(User user);

    UserBasic toUserBasic(User user);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserRequest request);


}
