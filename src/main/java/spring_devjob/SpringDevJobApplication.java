package spring_devjob;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// Disable Security
// @SpringBootApplication(exclude = {
// 		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
// 		org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
// })

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SpringDevJobApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory("./") // Chỉ định thư mục chứa .env
				.load();
		// Nạp tất cả biến từ .env vào System Properties
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(SpringDevJobApplication.class, args);
	}

}
