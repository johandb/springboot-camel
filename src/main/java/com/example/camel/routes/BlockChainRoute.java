package com.example.camel.routes;

import com.example.camel.model.Operation;
import com.example.camel.model.Response;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;

import static org.apache.camel.component.web3j.Web3jConstants.*;

@Component
@Slf4j
public class BlockChainRoute extends RouteBuilder {

    String topics = EventEncoder.buildEventSignature("setBTCCap()");

    @Value("${web3.host.url}")
    private String WEB3_URL;

    @Value("${web3.host.port}")
    private String WEB3_PORT;

    private Gson gson = new Gson();

    @Override
    public void configure() throws Exception {
        log.info("topic:{}", topics);

        from("activemq:queue:operation")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    log.info("BODY:{}", body);
                    Operation operation = gson.fromJson(body, Operation.class);
                    exchange.getIn().setHeader("operation", operation.getOperation());
                    exchange.getIn().setHeader("txdata", operation.getTxData());
                })
                // We moeten ToD gebruiken voor dynamische routing want to geeft exceptions
                .toD(WEB3_URL + ":" + WEB3_PORT + "?operation=${header.operation}${header.txdata}")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    String operation = (String) exchange.getIn().getHeader("operation");
                    Response response = new Response(operation, body);
                    String json = gson.toJson(response);
                    exchange.getIn().setBody(json);
                    log.info("RESPONSE:{}", json);
                })
                .to("websocket://localhost:8000/ws?sendToAll=true")
                .end();

        from(WEB3_URL + ":" + WEB3_PORT + "?operation=TRANSACTION_OBSERVABLE")
                .process(exchange -> {
                    EthBlock.TransactionObject transactionObject = exchange.getIn().getBody(EthBlock.TransactionObject.class);
                    log.info("TRANSACTION BLOCK:{}", transactionObject.get().getBlockNumber().toString());
                    exchange.getIn().setBody(transactionObject.get().getBlockNumber().toString());
                })
                .to("activemq:queue:blocks")
                .end();

        from("activemq:queue:blocks")
                .process(exchange -> {
                    String blockNumber = exchange.getIn().getBody(String.class);
                    exchange.getIn().setHeader("operation", "ETH_GET_BLOCK_BY_NUMBER");
                    exchange.getIn().setHeader("txdata", "atBlock=" + blockNumber + "&fullTransactionObjects=true");
                })
                .toD(WEB3_URL + ":" + WEB3_PORT + "?operation=${header.operation}&${header.txdata}")
                .process(exchange -> {
                    EthBlock.Block block = exchange.getIn().getBody(EthBlock.Block.class);
                    LocalDateTime stamp = Instant.ofEpochMilli(block.getTimestamp().longValue() * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    log.info("BLOCK {} MINED ON:{}", block.getNumber().toString(), stamp.toString());
                })
                .end();

        from("activemq:queue:oracle")
//        from("web3j://http://127.0.0.1:7545?operation=ETH_LOG_OBSERVABLE")
                .setHeader(OPERATION, constant(ETH_SEND_TRANSACTION))
                .setHeader(FROM_ADDRESS, constant("0x5f5e3241bbbE86e03e1a9f76879Fbd29ddf21DB2"))
                .setHeader(TO_ADDRESS, constant("0x902750d2cee0B229A135E0f4B48279d02BB55453"))
                .setHeader(AT_BLOCK, constant("latest"))
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        //int random = new Random().nextInt(50);
                        //log.info("setConsent:{}", random);
                        Function function = new Function("setConsent",
                                Arrays.<Type>asList(new Utf8String("1f4e22d9b6cc84622293adc79d3ba4e55721d99924616307ee4b59cb543162d9"), new Bool(true)),
                                Collections.<TypeReference<?>>emptyList());
                        String setConsent = FunctionEncoder.encode(function);
                        exchange.getIn().setHeader(DATA, setConsent);
                    }
                })
                .to("web3j://http://127.0.0.1:7545")
//                .process(exchange -> {
//                    log.info("TX:{}", exchange.getIn().getBody());
//                })
                .end();
    }
}
