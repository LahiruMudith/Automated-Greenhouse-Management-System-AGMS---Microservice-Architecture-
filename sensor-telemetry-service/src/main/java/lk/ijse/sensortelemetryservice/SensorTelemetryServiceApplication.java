package lk.ijse.sensortelemetryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SensorTelemetryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorTelemetryServiceApplication.class, args);
	}

}
