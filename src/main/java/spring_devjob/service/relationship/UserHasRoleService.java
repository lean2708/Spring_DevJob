package spring_devjob.service.relationship;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.RoleEnum;
import spring_devjob.entity.Role;
import spring_devjob.entity.User;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.RoleRepository;
import spring_devjob.repository.relationship.UserHasRoleRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHasRoleService {
    private final UserHasRoleRepository userHasRoleRepository;
    private final RoleRepository roleRepository;

    public UserHasRole saveUserHasRole(User user, RoleEnum roleEnum){
        Role role = roleRepository.findByName(roleEnum.name())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        return userHasRoleRepository.save(new UserHasRole(user, role));
    }

    public void deleteUserHasRoleByRole(Long roleId){
        List<UserHasRole> userHasRoleList = userHasRoleRepository.findByRoleId(roleId);
        userHasRoleRepository.deleteAll(userHasRoleList);
    }

    public void updateUserHasRole(UserHasRole userHasRole, EntityStatus status){
        userHasRole.setState(status);
        userHasRoleRepository.save(userHasRole);
    }

    public List<UserHasRole> getUserHasRoleByUserAndState(Long userId, EntityStatus status){
        return userHasRoleRepository.findByUserIdAndState(userId, status.name());
    }
}
