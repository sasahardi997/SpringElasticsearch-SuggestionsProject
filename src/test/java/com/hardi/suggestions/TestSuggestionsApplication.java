package com.hardi.suggestions;

import org.springframework.boot.SpringApplication;

public class TestSuggestionsApplication {

	public static void main(String[] args) {
		SpringApplication.from(SuggestionsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
