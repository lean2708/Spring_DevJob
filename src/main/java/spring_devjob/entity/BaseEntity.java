package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import spring_devjob.service.AuthService;

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
    String name;

    LocalDate createdAt;
    LocalDate updatedAt;
    String createdBy;
    String updatedBy;

    @PrePersist
    public void hanldeBeforeCreate(){
        this.createdBy = AuthService.getCurrentUserLogin();
        this.createdAt = LocalDate.now();
    }

    @PreUpdate
    public void hanldeBeforeUpdate(){
        this.updatedBy = AuthService.getCurrentUserLogin();
        this.updatedAt = LocalDate.now();
    }
}
