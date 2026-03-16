package com.vitaveda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VitaVedaApplication {

	public static void main(String[] args) {
		SpringApplication.run(VitaVedaApplication.class, args);
	}

}
