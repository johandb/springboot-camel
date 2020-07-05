package com.example.camel.services;

import com.example.camel.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final Web3j web3j;

    public String sentTransaction(Transaction transaction) {
        return "tx hashdata";
    }
}
