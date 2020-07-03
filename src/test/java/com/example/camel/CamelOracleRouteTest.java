/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.camel.component.web3j.Web3jConstants.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CamelOracleRouteTest extends CamelTestSupport {

    @Produce(uri = "web3j://http://127.0.0.1:7545")
    protected ProducerTemplate template;

    private final String addressFrom = "0x6e62f007992992DC7e0EA18208DCe4E273F8b898";
    private final String addressTo = "0x42fC663a724C5792CC22ca6DcF026c626d322618";


    @Test
    public void getBTCCap() throws Exception {
        Function function = new Function("getBTCCap", Arrays.<Type>asList(), Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {
        }));
        String encodedFunction = FunctionEncoder.encode(function);

        Exchange exchange = createExchangeWithHeader(OPERATION, ETH_CALL);
        exchange.getIn().setHeader(FROM_ADDRESS, addressFrom);
        exchange.getIn().setHeader(TO_ADDRESS, addressTo);
        exchange.getIn().setHeader(AT_BLOCK, "latest");
        exchange.getIn().setHeader(DATA, encodedFunction);

        template.send(exchange);
        String body = exchange.getIn().getBody(String.class);
        assertTrue(body != null);

        Type result = FunctionReturnDecoder.decodeIndexedValue(body, new TypeReference<Uint>() {
        });
        List<Type> decode = FunctionReturnDecoder.decode(body, function.getOutputParameters());
        System.out.println("getBTCCap: " + decode.get(0).getValue());
    }

    @Test
    public void updateBTCCap() throws Exception {
        Function function = new Function("updateBTCCap", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        String encodedFunction = FunctionEncoder.encode(function);

        Exchange exchange = createExchangeWithHeader(OPERATION, ETH_SEND_TRANSACTION);
        exchange.getIn().setHeader(FROM_ADDRESS, addressFrom);
        exchange.getIn().setHeader(TO_ADDRESS, addressTo);
        exchange.getIn().setHeader(AT_BLOCK, "latest");
        exchange.getIn().setHeader(DATA, encodedFunction);

        template.send(exchange);
        String body = exchange.getIn().getBody(String.class);
        assertTrue(body != null);

        List<Type> decode = FunctionReturnDecoder.decode(body, function.getOutputParameters());
        System.out.println("transaction hash: " + body);
        System.out.printf("transaction result  " + decode);
    }

    @Test
    public void setBTCCap() throws Exception {
        Function function = new Function("setBTCCap", Arrays.<Type>asList(new Uint(BigInteger.valueOf(100))), Collections.<TypeReference<?>>emptyList());
        String encodedFunction = FunctionEncoder.encode(function);

        Exchange exchange = createExchangeWithHeader(OPERATION, ETH_SEND_TRANSACTION);
        exchange.getIn().setHeader(FROM_ADDRESS, addressFrom);
        exchange.getIn().setHeader(TO_ADDRESS, addressTo);
        exchange.getIn().setHeader(AT_BLOCK, "latest");
        exchange.getIn().setHeader(DATA, encodedFunction);

        template.send(exchange);
        String body = exchange.getIn().getBody(String.class);
        assertTrue(body != null);

        List<Type> decode = FunctionReturnDecoder.decode(body, function.getOutputParameters());
        System.out.println("transaction hash: " + body);
        System.out.printf("transaction result  " + decode.toString());
    }

    @Test
    public void setConsent() throws Exception {
        // 1f4e22d9b6cc84622293adc79d3ba4e55721d99924616307ee4b59cb543162d9
        Function function = new Function("setConsent",
                Arrays.<Type>asList(new Utf8String("1f4e22d9b6cc84622293adc79d3ba4e55721d99924616307ee4b59cb543162d9"), new Bool(true)),
                Arrays.<TypeReference<?>>asList());
        String encodedFunction = FunctionEncoder.encode(function);

        Exchange exchange = createExchangeWithHeader(OPERATION, ETH_SEND_TRANSACTION);
        exchange.getIn().setHeader(FROM_ADDRESS, addressFrom);
        exchange.getIn().setHeader(TO_ADDRESS, addressTo);
        exchange.getIn().setHeader(AT_BLOCK, "latest");
        exchange.getIn().setHeader(DATA, encodedFunction);

        template.send(exchange);
        String body = exchange.getIn().getBody(String.class);
        System.out.println("body:" + body);
        assertTrue(body != null);

        List<Type> decode = FunctionReturnDecoder.decode(body, function.getOutputParameters());
        System.out.println("transaction hash: " + body);
        System.out.printf("transaction result  " + decode.toString());
    }

    @Test
    public void getConsent() throws Exception {
        Function function = new Function("getConsent", Arrays.<Type>asList(), Arrays.<TypeReference<?>>asList(new TypeReference<Uint>() {
        }));
        String encodedFunction = FunctionEncoder.encode(function);

        Exchange exchange = createExchangeWithHeader(OPERATION, ETH_CALL);
        exchange.getIn().setHeader(FROM_ADDRESS, addressFrom);
        exchange.getIn().setHeader(TO_ADDRESS, addressTo);
        exchange.getIn().setHeader(AT_BLOCK, "latest");
        exchange.getIn().setHeader(DATA, encodedFunction);

        template.send(exchange);
        String body = exchange.getIn().getBody(String.class);
        System.out.println("body:" + body);
        assertTrue(body != null);

        Type result = FunctionReturnDecoder.decodeIndexedValue(body, new TypeReference<Uint>() {
        });
        List<Type> decode = FunctionReturnDecoder.decode(body, function.getOutputParameters());
        System.out.println("getConsent: " + decode.get(0).getValue());
    }

    protected Exchange createExchangeWithHeader(String key, Object value) {
        DefaultExchange exchange = new DefaultExchange(context);
        exchange.getIn().setHeader(key, value);
        return exchange;
    }
}
