package spring_devjob.mapper;

import org.mapstruct.*;
import org.springframework.context.annotation.DependsOn;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.RoleResponse;
import spring_devjob.entity.Role;
import spring_devjob.entity.relationship.UserHasRole;

import java.util.List;


@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {PermissionMapper.class})
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    @Mapping(source = "permissions", target = "permissions", qualifiedByName = "roleHasPermissionToEntityBasic")
    RoleResponse toRoleResponse(Role role);

    List<RoleResponse> toRoleResponseList(List<Role> roles);

    @Mapping(target = "permissions", ignore = true)
    void updateRole(@MappingTarget Role role, RoleRequest request);

    @Mapping(target = "id", source = "userHasRole.role.id")
    @Mapping(target = "name", source = "userHasRole.role.name")
    @Named("userHasRoleToEntityBasic")
    EntityBasic userHasRoleToEntityBasic(UserHasRole userHasRole);


}
