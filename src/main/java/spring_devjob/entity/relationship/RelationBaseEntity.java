package spring_devjob.entity.relationship;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.EntityStatus;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class RelationBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    EntityStatus state;

    @PrePersist
    private void prePersist() {
        if (state == null) {
            state = EntityStatus.ACTIVE;
        }
    }
}
