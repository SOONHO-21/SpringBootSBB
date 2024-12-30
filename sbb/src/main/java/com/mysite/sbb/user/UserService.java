package com.mysite.sbb.user;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public SiteUser create(String username, String email, String password) {
		SiteUser user = new SiteUser();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		this.userRepository.save(user);
		return user;
	}
	
	//overloading 소셜 로그인
	public SiteUser create(String registrationId, String userName,
			String email, String password) {
		SiteUser user = new SiteUser();
		user.setUsername(userName);
		user.setEmail(email);
		user.setPassword(password);
		user.setRegisterId(registrationId);	  //추가
		this.userRepository.save(user);
		return user;
	}

	public SiteUser getUser(String username) {
		Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
		if (siteUser.isPresent()) {
			return siteUser.get();
		} else {
			throw new DataNotFoundException("siteuser not found");
		}
	}
	
    public SiteUser socialLogin(String registrationId, String username, String email) {
		Optional<SiteUser> user = this.userRepository.findByusername(username);
		if (user.isPresent()) {
		    return user.get();
		} else {
		    return this.create(registrationId, username, email, "");
		}
    }
    
    public Optional<SiteUser> findByEmail(String email) {
        return userRepository.findByEmail(email); // UserRepository의 findByEmail 호출
    }
}