package com.exemple.security.tools;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		String generatedSecuredPasswordHash = BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(12));
		return generatedSecuredPasswordHash;
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
	}

}
