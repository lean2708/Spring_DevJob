package spring_devjob.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_revoked_token")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevokedToken {
    @Id
    String id;
    Date expiryTime;
}