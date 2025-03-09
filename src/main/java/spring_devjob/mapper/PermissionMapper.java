package spring_devjob.mapper;

import org.mapstruct.Mapper;
import spring_devjob.dto.response.PermissionResponse;
import spring_devjob.entity.Permission;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toPermissionResponse(Permission permission);

}
