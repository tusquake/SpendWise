package com.finance.aiexpense;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.ai.vertex.ai.gemini.project-id=aiexpense-475508",
		"spring.ai.vertex.ai.gemini.location=us-central1"
})
class AiexpenseApplicationTests {

	@Test
	void contextLoads() {
	}

}

