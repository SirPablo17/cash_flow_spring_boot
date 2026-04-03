package pablo.nasc.cash_flow_spring_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CashFlowSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashFlowSpringBootApplication.class, args);
	}

}
