package com.example.camel.api;

import com.example.camel.model.Operation;
import com.example.camel.services.OperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/operation")
@CrossOrigin
public class OperationController {

    private final OperationService operationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void sentOperation(@RequestBody Operation operation) throws Exception {
        log.info("#sentOperation() - received operation:{}", operation);
        operationService.sentMessage(operation);
    }

    // localhost:8080/api/v1/operation/consent  { "operation": "", "txData": "true | false"
    @PostMapping(value = "/consent", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void sentConsent(@RequestBody Operation operation) throws Exception {
        log.info("#sentConsent() - received operation:{}", operation);
        operationService.sentConsent(operation);
    }


}
