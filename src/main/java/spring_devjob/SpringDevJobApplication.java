package spring_devjob;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// Disable Security
// @SpringBootApplication(exclude = {
// 		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
// 		org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
// })

@EnableScheduling
@EnableFeignClients
@SpringBootApplication
public class SpringDevJobApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(System.getProperty("user.dir"))
				.load();
		dotenv.entries().forEach(
				entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(SpringDevJobApplication.class, args);
	}

}
