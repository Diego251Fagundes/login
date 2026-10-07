package br.com.loginseguro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.session.mongodb.enabled=false",
		"spring.data.mongodb.auto-index-creation=false",
		"spring.mongodb.uri=mongodb://localhost:27017/login_seguro_test"
})
class LoginSeguroApplicationTests {

	@Test
	void contextLoads() {
	}

}
