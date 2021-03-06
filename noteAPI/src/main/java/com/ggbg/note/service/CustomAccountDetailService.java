package com.ggbg.note.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.ggbg.note.domain.entity.AccountEntity;
import com.ggbg.note.repository.AccountRepo;

@Component
public class CustomAccountDetailService implements UserDetailsService {
   
    @Autowired
    private AccountRepo accountRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AccountEntity> optional = accountRepo.findAccountByEmail(email);
		AccountEntity account = new AccountEntity();
		if(optional.isPresent()) {
			account = optional.get();
		}else {
			System.out.println("email not found"); // 해당부분에 대해서는 예외처리로 
		}
        List<GrantedAuthority> roles = new ArrayList<>();

        if (account == null) {
            throw new UsernameNotFoundException("account not found with email: " + email);
        }
        if ((account.getRoleKey()).equals("ROLE_ADMIN")) {
            roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return new User(account.getEmail(), account.getPassword(), roles);
    }
}
