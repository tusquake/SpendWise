package com.finance.aiexpense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class AiexpenseApplication {

	public static void main(String[] args) {
		// Fix DATABASE_URL BEFORE Spring Boot starts
		fixDatabaseUrl();

		try {
			SpringApplication.run(AiexpenseApplication.class, args);
		} catch (Exception e) {
			System.err.println("❌ Application failed to start:");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Fix Render's DATABASE_URL format BEFORE Spring Boot initialization
	 * This runs before @PostConstruct
	 */
	private static void fixDatabaseUrl() {
		System.out.println("=================================");
		System.out.println("🔍 Checking DATABASE_URL...");
		System.out.println("=================================");

		String dbUrl = System.getenv("DATABASE_URL");
		String springProfile = System.getenv("SPRING_PROFILE");

		System.out.println("📌 SPRING_PROFILE: " + springProfile);
		System.out.println("📌 DATABASE_URL exists: " + (dbUrl != null));

		if (dbUrl != null) {
			System.out.println("📌 Original DATABASE_URL: " + maskPassword(dbUrl));

			if (dbUrl.startsWith("postgres://")) {
				// Render's format: postgres://user:pass@host:port/db
				String jdbcUrl = dbUrl.replace("postgres://", "jdbc:postgresql://");
				System.setProperty("spring.datasource.url", jdbcUrl);
				System.out.println("✅ Converted to JDBC format");
				System.out.println("📌 New URL: " + maskPassword(jdbcUrl));
			} else if (dbUrl.startsWith("postgresql://")) {
				// Alternative format: postgresql://user:pass@host:port/db
				String jdbcUrl = dbUrl.replace("postgresql://", "jdbc:postgresql://");
				System.setProperty("spring.datasource.url", jdbcUrl);
				System.out.println("✅ Converted to JDBC format");
				System.out.println("📌 New URL: " + maskPassword(jdbcUrl));
			} else if (dbUrl.startsWith("jdbc:postgresql://")) {
				System.setProperty("spring.datasource.url", dbUrl);
				System.out.println("✅ Already in JDBC format");
			} else {
				System.err.println("⚠️ Unknown DATABASE_URL format: " + maskPassword(dbUrl));
			}

			// Also extract and set username/password if they're in the URL
			extractCredentials(dbUrl);
		} else {
			System.out.println("⚠️ DATABASE_URL not found. Using application.yml configuration.");
		}

		System.out.println("=================================\n");
	}

	/**
	 * Extract username and password from DATABASE_URL if present
	 */
	private static void extractCredentials(String dbUrl) {
		try {
			// Format: postgres://username:password@host:port/database
			if (dbUrl.contains("@")) {
				String credentials = dbUrl.split("@")[0];
				if (credentials.contains("://")) {
					credentials = credentials.split("://")[1];
				}
				if (credentials.contains(":")) {
					String[] parts = credentials.split(":", 2);
					String username = parts[0];
					String password = parts[1];

					System.setProperty("spring.datasource.username", username);
					System.setProperty("spring.datasource.password", password);

					System.out.println("✅ Extracted username: " + username);
					System.out.println("✅ Extracted password: ****");
				}
			}
		} catch (Exception e) {
			System.err.println("⚠️ Could not extract credentials from URL: " + e.getMessage());
		}
	}

	/**
	 * Mask password in URL for logging
	 */
	private static String maskPassword(String url) {
		if (url == null) return null;
		// Replace password with ****
		return url.replaceAll(":(.*?)@", ":****@");
	}
}