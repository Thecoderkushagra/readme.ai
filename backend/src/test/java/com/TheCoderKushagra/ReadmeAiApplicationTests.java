package com.thecoderkushagra;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "DB_URL=jdbc:postgresql://localhost:5432/dummy",
    "DB_USER=dummy",
    "DB_PASS=dummy",
    "spring.jpa.hibernate.ddl-auto=none",
    "JWT_SECRET=dummysecretkeythatisatleast32byteslongforhs256",
    "JWT_ACCESS_EXPIRATION=3600000",
    "JWT_REFRESH_EXPIRATION=86400000"
})
class ReadmeAiApplicationTests {

	@Test
	void contextLoads() {
	}

}
