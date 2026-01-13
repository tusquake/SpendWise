package com.finance.aiexpense;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;

@Slf4j
@SpringBootApplication
public class AiexpenseApplication {

	public static void main(String[] args) {

		printEnvironmentInfo();
		fixDatabaseUrl();

		try {
			SpringApplication.run(AiexpenseApplication.class, args);
			log.info("Application started successfully!");
		} catch (Exception e) {
			log.error("Application failed to start!", e);
			log.error("Error: {}", e.getMessage());
			System.exit(1);
		}
	}

	@PostConstruct
	public void loadGoogleCredentials() throws IOException {
		String credsJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
		if (credsJson != null) {
			Path temp = Files.createTempFile("gcp-creds", ".json");
			Files.writeString(temp, credsJson);
			System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", temp.toString());
			log.info("Google credentials loaded successfully (temp file: {})", temp);
		} else {
			log.warn("GOOGLE_APPLICATION_CREDENTIALS_JSON not found in environment!");
		}
	}

	private static void printEnvironmentInfo() {
		String springProfilesActive = System.getenv("SPRING_PROFILES_ACTIVE");
		String port = System.getenv("PORT");

		log.info("SPRING_PROFILES_ACTIVE: {}", springProfilesActive != null ? springProfilesActive : "not set");
		log.info("Server Port: {}", port != null ? port : "8080 (default)");

		String wrongVariable = System.getenv("SPRING_PROFILE");
		if (wrongVariable != null) {
			log.warn("WARNING: Found SPRING_PROFILE={}", wrongVariable);
			log.warn("This is WRONG! Change it to SPRING_PROFILES_ACTIVE in Render");
		}
	}

	private static void fixDatabaseUrl() {
		log.info("Database Configuration:");

		String dbUrl = System.getenv("DATABASE_URL");
		String pgHost = System.getenv("PGHOST");
		String pgPort = System.getenv("PGPORT");
		String pgDatabase = System.getenv("PGDATABASE");
		String pgUser = System.getenv("PGUSER");
		String pgPassword = System.getenv("PGPASSWORD");

		if (dbUrl != null) {
			log.info("Found DATABASE_URL: {}", maskPassword(dbUrl));

			if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
				String jdbcUrl = dbUrl
						.replace("postgres://", "jdbc:postgresql://")
						.replace("postgresql://", "jdbc:postgresql://");

				System.setProperty("spring.datasource.url", jdbcUrl);
				log.info("Converted to: {}", maskPassword(jdbcUrl));
				extractCredentials(dbUrl);
			} else if (dbUrl.startsWith("jdbc:postgresql://")) {
				System.setProperty("spring.datasource.url", dbUrl);
				log.info("Already in JDBC format");
			}
		} else if (pgHost != null && pgPort != null && pgDatabase != null) {
			String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDatabase);
			log.info("Using PG variables:");
			log.info("- PGHOST: {}", pgHost);
			log.info("- PGPORT: {}", pgPort);
			log.info("- PGDATABASE: {}", pgDatabase);
			log.info("- PGUSER: {}", pgUser);
			log.info("Constructed URL: {}", jdbcUrl);
		} else {
			log.info("Using configuration from application.yml");
		}
	}

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

					log.info("Username: {}", username);
					log.info("Password: ****");
				}
			}
		} catch (Exception e) {
			log.warn("Could not extract credentials: {}", e.getMessage());
		}
	}

	private static String maskPassword(String url) {
		if (url == null) return null;
		return url.replaceAll(":(.*?)@", ":****@");
	}
}
