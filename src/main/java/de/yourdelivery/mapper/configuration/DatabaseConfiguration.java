package de.yourdelivery.mapper.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.yourdelivery.mapper.DatabaseWorker;

@Configuration
public class DatabaseConfiguration {

	@Bean
	public de.yourdelivery.mapper.beans.mapping.Configuration configuration() {
		return new de.yourdelivery.mapper.beans.mapping.Configuration();
	}
	
	@Bean
	public DatabaseWorker databaseworker(){
		return new DatabaseWorker();
	}
}
