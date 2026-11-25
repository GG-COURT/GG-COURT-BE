package com.ggcourt;

import org.springframework.boot.SpringApplication;

public class TestGgCourtApplication {

	public static void main(String[] args) {
		SpringApplication.from(GgCourtApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
