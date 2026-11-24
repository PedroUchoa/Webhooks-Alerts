package com.pedro.WebHooksAlerts;

import org.springframework.boot.SpringApplication;

public class TestWebHooksAlertsApplication {

	public static void main(String[] args) {
		SpringApplication.from(WebHooksAlertsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
