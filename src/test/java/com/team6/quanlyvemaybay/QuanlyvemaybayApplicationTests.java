package com.team6.quanlyvemaybay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=test-only-secret-with-at-least-32-bytes")
class QuanlyvemaybayApplicationTests {

	@Test
	void contextLoads() {
	}

}
