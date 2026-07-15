package com.thecoderkushagra;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "DB_URL=jdbc:postgresql://localhost:5432/dummy",
    "DB_USER=dummy",
    "DB_PASS=dummy",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
class ReadmeAiApplicationTests {

	@Test
	void contextLoads() {
	}

}
