package com.mysite.sbb.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SiteUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//@Column(unique = true)
	private String username;

	private String password;

	@Column(unique = true)
	private String email;
	
	private String registerId;	//소셜 로그인을 하는 방식을 담을 registerId를 추가
    
    public SiteUser() {
		// 기본 생성자에서 기본값을 설정할 수도 있습니다.
	    this.username = "defaultUser";
	    this.email = "defaultEmail@example.com";
	}
    
    @Builder
    public SiteUser(String username, String email) {
        this.username = username;
        this.email = email;
    }
}