package spring_devjob.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    private String email;
    @Column(nullable = false, length = 1024)
    private String accessToken;
    @Column(nullable = false, length = 1024)
    private String refreshToken;


    @CreationTimestamp
    LocalDate createdAt;
    @UpdateTimestamp
    LocalDate updatedAt;
}
