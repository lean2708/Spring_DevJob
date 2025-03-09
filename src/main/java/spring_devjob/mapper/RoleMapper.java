package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.RoleBasic;
import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.RoleResponse;
import spring_devjob.entity.Role;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    @Mapping(source = "permissions", target = "permissions")
    RoleResponse toRoleResponse(Role role);

    List<RoleBasic> toRoleBasics(List<Role> roles);

    @Mapping(target = "permissions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRole(@MappingTarget Role role, RoleRequest request);
}
