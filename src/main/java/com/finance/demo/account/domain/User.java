package com.finance.demo.account.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 계정 관리 컨텍스트의 사용자 애그리게이트 루트
 * 
 * 유비쿼터스 언어 정의:
 * - User: 시스템에 인증된 개인 (계정 관리 컨텍스트에서의 의미)
 * - 속성: 이메일, 이름, 가입일시
 * - 규칙: 이메일은 유일해야 함
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Email
    @NotBlank
    @Column(unique = true)
    private String email;
    
    @NotBlank
    private String name;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    protected User() {} // JPA를 위한 기본 생성자
    
    public User(String email, String name) {
        this.email = email;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }
    
    // 도메인 비즈니스 메서드
    public void updateProfile(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        this.name = newName;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
