package spring_devjob.constants;

import java.util.Set;

public enum RoleEnum {
    ADMIN,
    HR,
    PRO,
    USER;


    public static final Set<String> IMMUTABLE_SYSTEM_ROLES = Set.of(
            ADMIN.name(), HR.name(), PRO.name(), USER.name()
    );
}
