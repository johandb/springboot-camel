package com.example.camel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@SpringBootApplication
public class SpringCamelDemoApplication {

    @Value("${web3.host.url}")
    private String WEB3_URL;

    @Value("${web3.host.port}")
    private String WEB3_PORT;

    public static void main(String[] args) {
        SpringApplication.run(SpringCamelDemoApplication.class, args);
    }

    @Bean
    public Web3j createWeb3j() {
        Web3j web3 = Web3j.build(new HttpService(WEB3_URL + ":" + WEB3_PORT));
        return web3;
    }
}
