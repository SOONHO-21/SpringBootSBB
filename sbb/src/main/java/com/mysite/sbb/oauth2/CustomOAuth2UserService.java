package com.mysite.sbb.oauth2;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import jdk.jshell.spi.ExecutionControl;

import java.security.Principal;
import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserService userService;
	private final HttpSession httpSession;
	
	@Autowired
	public CustomOAuth2UserService(UserService userService, HttpSession httpSession) {
		this.userService = userService;
		this.httpSession = httpSession;
	}
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		//DefaultOAuth2User 서비스를 통해 User 정보를 가져와야 하기 때문에 대리자 생성
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);		//OAuth2UserService 정보 가져오기
		
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		Map<String, Object> attributes = oAuth2User.getAttributes();	//사용자에게 받은 정보가 Map 형태로 담겨 있음
	
		SiteUser user;
		try {	//로그인한 구글 계정 정보로부터 이메일과 이름을 가져오기 + socialLogin 함수 호출
			user = this.login(registrationId, attributes);
		} catch (ExecutionControl.NotImplementedException e) {
			throw new RuntimeException(e);
		}
		
		httpSession.setAttribute("user", new SessionUser(user));	// SessionUser는 직접 구현. user 정보를 가진 객체를
																	// "user"라는 키로 넣어 유저의 로그인 세션 정보를 만듦
		return new CustomOAuth2User(user.getUsername(), user.getEmail());
	}
	
    private SiteUser login(String registrationId, Map<String, Object> attributes) throws ExecutionControl.NotImplementedException {
    	String name, email;
    	
        if (registrationId.startsWith("google")) {
            name = (String) attributes.get("name");
            email = (String) attributes.get("email");
        } else if(registrationId.startsWith("naver")) {
        	// 응답 받은 사용자의 정보를 Map형태로 변경.
        	//네이버의 경우, 사용자 정보가 attributes 안에 있는 또 다른 하위 맵 response에 담겨 전달. 네이버의 응답구조에 맞춰준 것
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            name = (String) response.get("name");
            email = (String) response.get("email");
        } else {
        	throw new ExecutionControl.NotImplementedException("Implemented Google and Naver login only");
        }
        
        // 기존 사용자 검색: 이메일로 기존 사용자를 찾고 있으면 해당 사용자 반환, 없으면 새로 생성
		Optional<SiteUser> existingUser = userService.findByEmail(email);
		if (existingUser.isPresent()) {
			return existingUser.get(); // 기존 사용자가 있으면 해당 사용자 반환
		}
        
        // 기존 사용자가 없으면 새 사용자 생성
        return userService.socialLogin(registrationId, name, email);
    }
	
	class CustomOAuth2User extends SiteUser implements OAuth2User {
		public CustomOAuth2User(String username, String email) {	//상위 클래스 정보 초기화
			super(username, email);
		}
		
		@Override
		public Map<String, Object> getAttributes() {
			return null;
		}
		
		@Override
		public String getName() {	//사용자명 정보 가져오기
			return this.getUsername();
		}
		
		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {	//권한 부여
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
			return authorities;
		}
	}
}