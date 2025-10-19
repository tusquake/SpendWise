package com.finance.aiexpense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiexpenseApplication {

	public static void main(String[] args) {
		System.out.println("===========================================");
		System.out.println("🚀 Starting AI Expense Tracker");
		System.out.println("===========================================");

		// Print environment info
		printEnvironmentInfo();

		// Fix DATABASE_URL if provided by Render
		fixDatabaseUrl();

		System.out.println("===========================================\n");

		try {
			SpringApplication.run(AiexpenseApplication.class, args);
			System.out.println("\n✅ Application started successfully!");
		} catch (Exception e) {
			System.err.println("\n❌ Application failed to start!");
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Print environment information for debugging
	 */
	private static void printEnvironmentInfo() {
		// Check CORRECT Spring Boot variable name
		String springProfilesActive = System.getenv("SPRING_PROFILES_ACTIVE");
		String port = System.getenv("PORT");

		System.out.println("📌 SPRING_PROFILES_ACTIVE: " + (springProfilesActive != null ? springProfilesActive : "not set"));
		System.out.println("📌 Server Port: " + (port != null ? port : "8080 (default)"));

		// Also check if wrong variable name is being used
		String wrongVariable = System.getenv("SPRING_PROFILE");
		if (wrongVariable != null) {
			System.err.println("⚠️  WARNING: Found SPRING_PROFILE=" + wrongVariable);
			System.err.println("⚠️  This is WRONG! Change it to SPRING_PROFILES_ACTIVE in Render");
		}
	}

	/**
	 * Fix Render's DATABASE_URL format if provided
	 */
	private static void fixDatabaseUrl() {
		System.out.println("\n📊 Database Configuration:");

		String dbUrl = System.getenv("DATABASE_URL");
		String pgHost = System.getenv("PGHOST");
		String pgPort = System.getenv("PGPORT");
		String pgDatabase = System.getenv("PGDATABASE");
		String pgUser = System.getenv("PGUSER");
		String pgPassword = System.getenv("PGPASSWORD");

		if (dbUrl != null) {
			System.out.println("   Found DATABASE_URL: " + maskPassword(dbUrl));

			if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
				// Convert Render's format to JDBC format
				String jdbcUrl = dbUrl
						.replace("postgres://", "jdbc:postgresql://")
						.replace("postgresql://", "jdbc:postgresql://");

				System.setProperty("spring.datasource.url", jdbcUrl);
				System.out.println("   ✅ Converted to: " + maskPassword(jdbcUrl));

				// Extract credentials from URL
				extractCredentials(dbUrl);
			} else if (dbUrl.startsWith("jdbc:postgresql://")) {
				System.setProperty("spring.datasource.url", dbUrl);
				System.out.println("   ✅ Already in JDBC format");
			}
		} else if (pgHost != null && pgPort != null && pgDatabase != null) {
			// Using PG* environment variables (recommended)
			String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDatabase);
			System.out.println("   Using PG variables:");
			System.out.println("   - PGHOST: " + pgHost);
			System.out.println("   - PGPORT: " + pgPort);
			System.out.println("   - PGDATABASE: " + pgDatabase);
			System.out.println("   - PGUSER: " + pgUser);
			System.out.println("   ✅ Constructed URL: " + jdbcUrl);
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

					System.out.println("   ✅ Username: " + username);
					System.out.println("   ✅ Password: ****");
				}
			}
		} catch (Exception e) {
			System.err.println("   ⚠️  Could not extract credentials: " + e.getMessage());
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