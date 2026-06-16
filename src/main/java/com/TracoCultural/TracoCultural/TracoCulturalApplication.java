package com.TracoCultural.TracoCultural;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class TracoCulturalApplication {

	public static void main(String[] args) {
		SpringApplication.run(TracoCulturalApplication.class, args);
	}

}
