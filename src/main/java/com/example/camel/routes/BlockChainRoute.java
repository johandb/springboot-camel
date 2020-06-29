package com.example.camel.routes;

import com.example.camel.model.Operation;
import com.example.camel.model.Response;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class BlockChainRoute extends RouteBuilder {

    @Value("${web3.host.url}")
    private String WEB3_URL;

    @Value("${web3.host.port}")
    private String WEB3_PORT;

    private Gson gson = new Gson();

    @Override
    public void configure() throws Exception {
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
                    Date date = new Date(block.getTimestamp().longValue() * 1000);
                    Format format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    log.info("BLOCK {} MINED ON:{}", block.getNumber().toString(), format.format(date));
                })
                .end();
    }
}
