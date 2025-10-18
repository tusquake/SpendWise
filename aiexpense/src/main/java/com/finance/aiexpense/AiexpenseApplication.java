package com.finance.aiexpense;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiexpenseApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiexpenseApplication.class, args);
	}

	/**
	 * Fix Render's DATABASE_URL format automatically (postgresql:// → jdbc:postgresql://)
	 */
	@PostConstruct
	public void fixRenderDatabaseUrl() {
		String dbUrl = System.getenv("DATABASE_URL");
		if (dbUrl != null) {
			if (dbUrl.startsWith("postgresql://")) {
				String jdbcUrl = dbUrl.replace("postgresql://", "jdbc:postgresql://");
				System.setProperty("spring.datasource.url", jdbcUrl);
				System.out.println("✅ Converted Render DATABASE_URL to JDBC format:\n" + jdbcUrl);
			} else if (dbUrl.startsWith("jdbc:postgresql://")) {
				System.setProperty("spring.datasource.url", dbUrl);
				System.out.println("✅ Using provided JDBC URL directly.");
			}
		} else {
			System.out.println("⚠️ DATABASE_URL not found. Using application.yml datasource configuration.");
		}
	}
}
