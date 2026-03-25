package com.example.file.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String fullName;
    @Column (unique = true)
    private String email;
    private String phone;
    private String city;
    private Double  totalSalary;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
