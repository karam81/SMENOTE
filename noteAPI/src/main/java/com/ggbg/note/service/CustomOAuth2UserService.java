package com.ggbg.note.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ggbg.note.domain.OAuthAttributes;
import com.ggbg.note.domain.Role;
import com.ggbg.note.domain.Token;
import com.ggbg.note.domain.entity.AccountEntity;
import com.ggbg.note.repository.AccountRepo;
import com.ggbg.note.util.JwtTokenUtil;
/*
 * http://localhost:8080/noteAPI/oauth2/authorization/google
 */
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	@Autowired
	private AccountRepo accountRepo;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	private HttpServletResponse response;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		System.out.println(userRequest.toString());
		OAuth2UserService delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
				.getUserNameAttributeName();
		OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
				oAuth2User.getAttributes());

		/*
		 * access token create,
		 */
		AccountEntity account = saveOrUpdate(attributes);
		String email = account.getEmail();
		List<GrantedAuthority> roles = new ArrayList<>();
		if ((account.getRoleKey()).equals("ROLE_ADMIN")) {
			roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		} else {
			roles.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		final String accessToken = jwtTokenUtil.generateAccessToken(email, roles);
		final String refreshToken = jwtTokenUtil.generateRefreshToken(email, roles);

		ValueOperations<String, Object> vop = redisTemplate.opsForValue();
		Token token = new Token();
		token.setEmail(email);
		token.setToken(refreshToken);
		vop.set(email, token); // 일주일
		redisTemplate.expire(email, 60 * 60 * 24 * 31, TimeUnit.SECONDS); // 한달

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		Calendar accessTokenCal = Calendar.getInstance();
		accessTokenCal.add(Calendar.MINUTE, 30);

		Calendar refrestTokenCal = Calendar.getInstance();
		refrestTokenCal.add(Calendar.DATE, 30);
		
		String accessTokenExpirationDate = simpleDateFormat.format(accessTokenCal.getTime()); 
		String refreshTokenExpirationDate = simpleDateFormat.format(refrestTokenCal.getTime()); 
		
		response.addHeader("Authorization", "Bearer " + accessToken);
		response.addHeader("RefreshToken", "Bearer " + refreshToken);
		response.addHeader("AccessTokenExpiraionDate", accessTokenExpirationDate);
		response.addHeader("RefreshTokenExpiraionDate", refreshTokenExpirationDate);
		
		return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(account.getRoleKey())),
				attributes.getAttributes(), attributes.getNameAttributeKey());
	}

	private AccountEntity saveOrUpdate(OAuthAttributes attributes) {
		String email = attributes.getEmail();
		Optional<AccountEntity> optional = accountRepo.findAccountByEmail(email);
		AccountEntity account = new AccountEntity();
		if (optional.isPresent()) {
			account = optional.get();
		} else {
			account.setEmail(attributes.getEmail());
			account.setName(attributes.getName());
			account.setRole(Role.USER);
			accountRepo.save(account);
		}
		return account;
	}
}