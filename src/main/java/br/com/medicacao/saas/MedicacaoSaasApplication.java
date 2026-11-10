package br.com.medicacao.saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MedicacaoSaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicacaoSaasApplication.class, args);
    }
}
