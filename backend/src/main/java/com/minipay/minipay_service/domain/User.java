package com.minipay.minipay_service.domain;

import com.minipay.minipay_service.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @Column(nullable=false,unique=true,length=50)
    private String username;

    @Column(nullable=false, unique=true, length=100)
    private String email;

    @Column(nullable=false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private Role role = Role.USER;
}
