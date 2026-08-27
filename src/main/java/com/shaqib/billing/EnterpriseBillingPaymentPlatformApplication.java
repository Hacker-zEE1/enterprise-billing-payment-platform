package com.shaqib.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EnterpriseBillingPaymentPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnterpriseBillingPaymentPlatformApplication.class, args);
	}

}
