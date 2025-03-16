package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import spring_devjob.constants.EntityStatus;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    EntityStatus state;
    @Column(name = "deactivatedAt")
    LocalDate deactivatedAt;

    @CreationTimestamp
    LocalDate createdAt;
    @UpdateTimestamp
    LocalDate updatedAt;
    @CreatedBy
    String createdBy;
    @LastModifiedBy
    String updatedBy;

    @PrePersist
    public void prePersist() {
        if (this.state == null) {
            this.state = EntityStatus.ACTIVE;
        }
    }
}
