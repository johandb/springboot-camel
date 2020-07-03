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
import org.web3j.abi.datatypes.*;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static org.apache.camel.component.web3j.Web3jConstants.*;

@Component
@Slf4j
public class BlockChainRoute extends RouteBuilder {

    String topics = EventEncoder.buildEventSignature("Notarized()");

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

        from("activemq:queue:consent")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    log.info("BODY:{}", body);
                    Operation operation = gson.fromJson(body, Operation.class);
                    boolean consent = operation.getTxData().equalsIgnoreCase("true") ? true : false;
                    log.info("value: {}", consent);
                    Function function = new Function("setConsent",
                            Arrays.<Type>asList(new Utf8String("1f4e22d9b6cc84622293adc79d3ba4e55721d99924616307ee4b59cb543162d9"), new Bool(consent)),
                            Arrays.asList(new TypeReference<Bool>() {
                            }));
                    String encodedFunction = FunctionEncoder.encode(function);
                    exchange.getIn().setHeader(OPERATION, ETH_SEND_TRANSACTION);
                    exchange.getIn().setHeader(FROM_ADDRESS, "0x6e62f007992992DC7e0EA18208DCe4E273F8b898");
                    exchange.getIn().setHeader(TO_ADDRESS, "0x4232Dd27e975DA918363EbA39caC883f5312513b");
                    exchange.getIn().setHeader(AT_BLOCK, "latest");
                    exchange.getIn().setHeader(GAS_LIMIT, "6721975");
                    exchange.getIn().setHeader(GAS_PRICE, "21000");
                    exchange.getIn().setHeader(DATA, encodedFunction);
                })
                .to("web3j://http://127.0.0.1:7545")
                .process(exchange -> {
                    log.info("TX:{}", exchange.getIn().getBody());
                })
                .end();

        from("web3j://http://127.0.0.1:7545?operation=ETH_LOG_OBSERVABLE&topics=" + topics)
                .setHeader(OPERATION, constant(ETH_SEND_TRANSACTION))
                .setHeader(FROM_ADDRESS, constant("0x6e62f007992992DC7e0EA18208DCe4E273F8b898"))
                .setHeader(TO_ADDRESS, constant("0x4232Dd27e975DA918363EbA39caC883f5312513b"))
                .setHeader(AT_BLOCK, constant("latest"))
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        int random = new Random().nextInt(50);
                        log.info("ETH_LOG_OBSERVABLE: {}", exchange.getIn().getBody());
                        log.info("set amount:{}", random);
                        Function function = new Function("setBTCCap", Arrays.<Type>asList(new Uint(BigInteger.valueOf(random))), Collections.<TypeReference<?>>emptyList());
                        String setBTCCap = FunctionEncoder.encode(function);
                        exchange.getIn().setHeader(DATA, setBTCCap);
                    }
                })
                .to("web3j://http://127.0.0.1:7545")
                .process(exchange -> {
                    log.info("TX:{}", exchange.getIn().getBody());
                })
                .end();
    }

}
