package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.basic.RoleBasic;
import spring_devjob.dto.response.RoleResponse;
import spring_devjob.entity.Role;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);

    List<RoleBasic> toRoleBasics(List<Role> roles);

}
