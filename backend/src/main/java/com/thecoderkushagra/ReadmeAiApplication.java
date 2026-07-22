package com.thecoderkushagra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(excludeName = {
    "org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration",
    "org.springframework.ai.autoconfigure.ollama.OllamaChatAutoConfiguration"
})
public class ReadmeAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadmeAiApplication.class, args);
	}

}
