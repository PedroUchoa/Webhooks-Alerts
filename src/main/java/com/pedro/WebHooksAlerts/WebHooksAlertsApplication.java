package com.pedro.WebHooksAlerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebHooksAlertsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebHooksAlertsApplication.class, args);
	}

}
