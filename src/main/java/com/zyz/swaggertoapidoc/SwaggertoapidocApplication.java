package com.zyz.swaggertoapidoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SwaggertoapidocApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwaggertoapidocApplication.class, args);
    }





    @Bean
    public RestTemplate restTemplate(){
        System.out.println("111");
        return new RestTemplate();
    }
}
