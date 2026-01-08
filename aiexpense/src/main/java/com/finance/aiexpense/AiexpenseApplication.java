package com.finance.aiexpense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;

@SpringBootApplication
public class AiexpenseApplication {

	public static void main(String[] args) {
		printEnvironmentInfo();
		fixDatabaseUrl();
		try {
			SpringApplication.run(AiexpenseApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Load Google Cloud credentials from environment (for Render or Cloud)
	 */
	@PostConstruct
	public void loadGoogleCredentials() throws IOException {
		String credsJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
		if (credsJson != null) {
			Path temp = Files.createTempFile("gcp-creds", ".json");
			Files.writeString(temp, credsJson);
			System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", temp.toString());
			System.out.println("Google credentials loaded successfully (temp file: " + temp + ")");
		} else {
			System.err.println("GOOGLE_APPLICATION_CREDENTIALS_JSON not found in environment!");
		}
	}

	/**
	 * Print environment information for debugging
	 */
	private static void printEnvironmentInfo() {
		// Check CORRECT Spring Boot variable name
		String springProfilesActive = System.getenv("SPRING_PROFILES_ACTIVE");
		String port = System.getenv("PORT");

		System.out.println("SPRING_PROFILES_ACTIVE: " + (springProfilesActive != null ? springProfilesActive : "not set"));
		System.out.println("Server Port: " + (port != null ? port : "8080 (default)"));

		// Also check if wrong variable name is being used
		String wrongVariable = System.getenv("SPRING_PROFILE");
		if (wrongVariable != null) {
			System.err.println("WARNING: Found SPRING_PROFILE=" + wrongVariable);
			System.err.println("This is WRONG! Change it to SPRING_PROFILES_ACTIVE in Render");
		}
	}

	/**
	 * Fix Render's DATABASE_URL format if provided
	 */
	private static void fixDatabaseUrl() {

		String dbUrl = System.getenv("DATABASE_URL");
		String pgHost = System.getenv("PGHOST");
		String pgPort = System.getenv("PGPORT");
		String pgDatabase = System.getenv("PGDATABASE");
		String pgUser = System.getenv("PGUSER");
		String pgPassword = System.getenv("PGPASSWORD");

		if (dbUrl != null) {
			System.out.println("   Found DATABASE_URL: " + maskPassword(dbUrl));

			if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
				String jdbcUrl = dbUrl
						.replace("postgres://", "jdbc:postgresql://")
						.replace("postgresql://", "jdbc:postgresql://");

				System.setProperty("spring.datasource.url", jdbcUrl);
				extractCredentials(dbUrl);
			} else if (dbUrl.startsWith("jdbc:postgresql://")) {
				System.setProperty("spring.datasource.url", dbUrl);
			}
		} else if (pgHost != null && pgPort != null && pgDatabase != null) {
			String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDatabase);
		} else {
			System.out.println("   Using configuration from application.yml");
		}
	}

	/**
	 * Extract username and password from DATABASE_URL
	 */
	private static void extractCredentials(String dbUrl) {
		try {
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
				}
			}
		} catch (Exception e) {
			System.err.println(" Could not extract credentials: " + e.getMessage());
		}
	}

	/**
	 * Mask password in URL for logging
	 */
	private static String maskPassword(String url) {
		if (url == null) return null;
		return url.replaceAll(":(.*?)@", ":****@");
	}
}
