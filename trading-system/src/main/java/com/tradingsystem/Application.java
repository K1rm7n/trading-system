package com.tradingsystem;

import com.tradingsystem.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

/**
 * Главный класс приложения "Intelligent Investment Decision Support System"
 * Инициализирует и запускает Spring Boot приложение
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties
public class Application {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	/**
	 * Точка входа в приложение
	 *
	 * @param args аргументы командной строки
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		logger.info("Intelligent Investment Decision Support System started successfully");
	}

	/**
	 * Bean для логирования информации о запуске приложения
	 *
	 * @param applicationProperties настройки приложения
	 * @return CommandLineRunner для выполнения при запуске
	 */
	@Bean
	public CommandLineRunner startupInfoLogger(ApplicationProperties applicationProperties) {
		return args -> {
			logger.info("Application name: {}", applicationProperties.getName());
			logger.info("Application version: {}", applicationProperties.getVersion());
			logger.info("Cache enabled: {}", applicationProperties.isCacheEnabled());
			logger.info("Scheduled updates enabled: {}", applicationProperties.isScheduledUpdatesEnabled());
		};
	}

	/**
	 * Bean для отображения списка зарегистрированных бинов (полезно для отладки)
	 *
	 * @param ctx контекст приложения
	 * @return CommandLineRunner для выполнения при запуске
	 */
	@Bean
	public CommandLineRunner beanInfoLogger(ApplicationContext ctx) {
		return args -> {
			if (logger.isDebugEnabled()) {
				logger.debug("Inspecting all beans provided by Spring Boot:");
				String[] beanNames = ctx.getBeanDefinitionNames();
				Arrays.sort(beanNames);
				for (String beanName : beanNames) {
					logger.debug("Bean: {}", beanName);
				}
			}
		};
	}

	/**
	 * Bean для инициализации данных при первом запуске приложения
	 *
	 * @return CommandLineRunner для выполнения при запуске
	 */
	@Bean
	public CommandLineRunner initialDataSetup() {
		return args -> {
			logger.info("Running initial data setup checks...");
			// Здесь может быть код для проверки и инициализации данных
			// Например, проверка наличия админ-пользователя и создание его при отсутствии
			// Или загрузка начального списка популярных акций, если база данных пуста
		};
	}
}
