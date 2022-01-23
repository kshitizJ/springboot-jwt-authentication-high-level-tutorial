package com.jwt.supportportal;

import static com.jwt.supportportal.constant.FileConstant.USER_FOLDER;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class SupportPortalApplication {

	public static void main(String[] args) { 
		SpringApplication.run(SupportPortalApplication.class, args);

		// Once the application starts, the user image folder will automatically get
		// created.
		new File(USER_FOLDER).mkdirs();

	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
