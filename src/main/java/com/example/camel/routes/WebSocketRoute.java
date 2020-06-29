package com.example.camel.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("websocket://localhost:8000/ws")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String body = exchange.getIn().getBody(String.class);
                        log.info("WS:{}", body);
                    }
                })
                .to("websocket://localhost:8000/ws")
                .end();
    }
}
