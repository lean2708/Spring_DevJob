package spring_devjob.entity.history;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseHistoryEntity {

    @Id
    long id;
    @Column(nullable = false)
    String name;

    @CreationTimestamp
    @Column(name = "archivedAt", nullable = false, updatable = false)
    LocalDateTime archivedAt;
}
