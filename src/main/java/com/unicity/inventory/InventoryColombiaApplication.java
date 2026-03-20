package com.unicity.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class InventoryColombiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryColombiaApplication.class, args);
	}

}
