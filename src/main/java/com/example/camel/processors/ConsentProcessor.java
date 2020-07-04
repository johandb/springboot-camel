package com.example.camel.processors;

import com.example.camel.model.Operation;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;

import java.util.Arrays;

import static org.apache.camel.component.web3j.Web3jConstants.*;

@Slf4j
public class ConsentProcessor implements Processor {

    private Gson gson = new Gson();

    @Override
    public void process(Exchange exchange) throws Exception {
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

    }
}
