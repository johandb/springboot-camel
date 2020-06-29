package com.example.camel.api;

import com.example.camel.model.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/operation")
@CrossOrigin
public class OperationController {

    private final JmsTemplate jmsTemplate;

    @Value("${activemq.queue.operation}")
    private String operationQueue;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void sentOperation(@RequestBody Operation operation) throws Exception {
        log.info("#sentOperation() - received operation:{}", operation);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(operation);
        log.info("Sending to queue {} - {}", operationQueue, json);
        jmsTemplate.convertAndSend(operationQueue, json);
    }
}
