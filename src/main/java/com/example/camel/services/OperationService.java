package com.example.camel.services;

import com.example.camel.model.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OperationService {

    private final JmsTemplate jmsTemplate;

    @Value("${activemq.queue.operation}")
    private String operationQueue;

    public void sentMessage(final Operation operation) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(operation);

        log.info("Sending to queue {} - {}", operationQueue, json);
        jmsTemplate.convertAndSend(operationQueue, json);
        
    }

}
