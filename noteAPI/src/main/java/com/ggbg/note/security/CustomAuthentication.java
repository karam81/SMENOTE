package com.ggbg.note.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.ggbg.note.domain.entity.AccountEntity;
import com.ggbg.note.repository.AccountRepo;
import com.ggbg.note.util.JwtTokenUtil;

@Component
public class CustomAuthentication {

	@Autowired
	private JwtTokenUtil jtu;

	@Autowired
	private AccountRepo accountRepo;

	public Authentication getAuthentication(String token) {
		Map<String, Object> parseInfo = jtu.getUserParseInfo(token);
		List<String> rs = (List) parseInfo.get("role");
		Collection<GrantedAuthority> tmp = new ArrayList<>();
		for (String a : rs) {
			tmp.add(new SimpleGrantedAuthority(a));
		}
		UserDetails userDetails = User.builder().username(String.valueOf(parseInfo.get("email"))).authorities(tmp)
				.password("xxxx").build();
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
		return usernamePasswordAuthenticationToken;
	}

//	public Authentication getAuthentication(String email, String password) {
//		BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder(10);
//		System.out.println("/");
//		System.out.println(email);
//		System.out.println(password);
//		Optional<Account> optional = accountRepo.getAccountByEmail(email);
//		boolean check = false;
//		Account account = new Account();
//		if (optional.isPresent()) {
//			account = optional.get();
//			check = bcryptPasswordEncoder.matches(password, account.getPassword());
//		} else { // 해당 아이디에 대한 정보가 없을때 fail return;
//			System.out.println("[Login] - email is null"); // 해당부분에 대해서는 예외처리로
//			return null;
//		}
//		if (!check) { // 비밀번호가 일치하지 않는 경우 fail return;
//			System.out.println("[Login] - password is null"); // 해당부분에 대해서는 예외처리로
//			return null;
//		}
//
//		List<GrantedAuthority> roles = new ArrayList<>();
//		if ((account.getRoleKey()).equals("ROLE_ADMIN")) {
//			roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
//		} else {
//			roles.add(new SimpleGrantedAuthority("ROLE_USER"));
//		}
//
//		UserDetails userDetails = User.builder().username(email).authorities(roles).password("xxxx").build();
//
//		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//	}
}
