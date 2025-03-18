package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.UserBasic;
import spring_devjob.dto.request.RegisterRequest;
import spring_devjob.dto.request.UserCreationRequest;
import spring_devjob.dto.request.UserUpdateRequest;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.User;
import spring_devjob.entity.history.UserHistory;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User userCreationToUser(UserCreationRequest request);

    User registerToUser(RegisterRequest request);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "userHasRoleToEntityBasic")
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    UserHistory toUserHistory(User user);

    List<UserHistory> toUserHistoryList(List<User> users);

    UserBasic toUserBasic(User user);

}
