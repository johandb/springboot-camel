package com.example.camel.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class Transaction {
    private String addressFrom;
    private String addressTo;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private int nonce;
    private BigInteger value;
}
