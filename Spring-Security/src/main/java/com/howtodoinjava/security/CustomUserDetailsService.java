package com.howtodoinjava.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService
{
	public UserDetails loadUserByUsername(String username)
	        throws UsernameNotFoundException, DataAccessException
    {
		System.out.println("username recieved :: " + username);
		@SuppressWarnings("deprecation")
		UserDetails user = new User(username, "password", true, true, true, true,
				new GrantedAuthority[]{ new GrantedAuthorityImpl("ROLE_USER") });
		return user;
    }
}
