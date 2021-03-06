package com.ggbg.note.security;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ggbg.note.domain.Token;
import com.ggbg.note.domain.dto.LoginViewDTO;
import com.ggbg.note.util.JwtTokenUtil;

/*
 * reference
 * https://johnmarc.tistory.com/74
 * https://velog.io/@minholee_93/Spring-Security-JWT-Security-Spring-Boot-10
 */
@CrossOrigin(origins = {"http://localhost:8081"})
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	
	public CustomUsernamePasswordAuthenticationFilter(JwtTokenUtil jwtTokenUtil, RedisTemplate<String, Object> redisTemplate) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.redisTemplate = redisTemplate;
    }
	
	private HashMap<String, String> jsonRequest = new HashMap<String, String>();;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		System.out.println("[attemptAuthentication] entered..");
		LoginViewDTO loginViewModel = new LoginViewDTO();
		
		if (!request.getMethod().equals("POST"))
			throw new AuthenticationServiceException("Authentication method not supported " + request.getMethod());

		try {
			loginViewModel = new ObjectMapper().readValue(request.getInputStream(), LoginViewDTO.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * UsernamePasswordAuthenticationFilter class 에서는 default 방식이 form 태그로 넘어온 데이터를
		 * 받아서 수행하기때문에 json 으로 받도록 오버라이딩해줘야한다.
		 */
		
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				loginViewModel.getEmail(),
				loginViewModel.getPassword(),
				new ArrayList<>()
				);
		
		return this.getAuthenticationManager().authenticate(authenticationToken);
	}

	@SuppressWarnings("unchecked") // authResult 는 항상 successful 한 상황에서만 사용할 수 있으므로 반환되는값의 roles 를 가져올 수 있다.
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		System.out.println("[successfulAuthentication] entered....");
		
		
		String email = authResult.getName();
		List<GrantedAuthority> roles = new ArrayList<>();
		roles.add(new SimpleGrantedAuthority("ROLE_USER"));
		// jwt
//		System.out.println(jwtTokenUtil.generateAccessToken(email, roles));
		final String accessToken = jwtTokenUtil.generateAccessToken(email, roles);
		final String refreshToken = jwtTokenUtil.generateRefreshToken(email, roles);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		Calendar accessTokenCal = Calendar.getInstance(Locale.KOREA);
		accessTokenCal.add(Calendar.MINUTE, 30);

		Calendar refrestTokenCal = Calendar.getInstance(Locale.KOREA);
		refrestTokenCal.add(Calendar.DATE, 30);
		
		String accessTokenExpirationDate = simpleDateFormat.format(accessTokenCal.getTime()); 
		String refreshTokenExpirationDate = simpleDateFormat.format(refrestTokenCal.getTime()); 

		
		ValueOperations<String, Object> vop = redisTemplate.opsForValue();
		Token token = new Token();
		token.setEmail(email);
		token.setToken(refreshToken);
		vop.set(email, token); // 일주일
		redisTemplate.expire(email, 60 * 60 * 24 * 31, TimeUnit.SECONDS); // 한달

		// 로그인했을떄는 계속 refresh  token 만들어주고 / refresh token 이 살아있는동안은 access token 과
		// refresh token 의 값을 이용해서 access token 갱신
		// 그럼 쿠키에 담긴 access token 의 expire은 refresh token 과 같이 하는건지?

//		Cookie accessCookie = new Cookie("access-token", accessToken);
//		accessCookie.setMaxAge(10 * 60); // 10분
//		accessCookie.setPath("/");
//		response.addCookie(accessCookie); // path 설정 / 기간 설정
//	
//		Cookie refreshCookie = new Cookie("refresh-token", refreshToken);
//		refreshCookie.setMaxAge(60 * 60 * 24 * 31); // 한달
//		refreshCookie.setPath("/");
//		response.addCookie(refreshCookie);
		// jwt

		response.setHeader("Access-Control-Expose-Headers", "Authorization, RefreshToken, AccessTokenExpiraionDate, RefreshTokenExpiraionDate");
		
		response.addHeader("Authorization", "Bearer " + accessToken);
		response.addHeader("RefreshToken", "Bearer " + refreshToken);
		response.addHeader("AccessTokenExpiraionDate", accessTokenExpirationDate);
		response.addHeader("RefreshTokenExpiraionDate", refreshTokenExpirationDate);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		super.unsuccessfulAuthentication(request, response, failed);
	}
}
