package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.PaymentTx;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentTxRepositoryInMemory implements PaymentTxRepository {

	private static AtomicInteger transactionIdCounter = new AtomicInteger();
	private final Map<Integer, PaymentTx> paymentTransactions = new ConcurrentHashMap<>();

	@Override
	public void createPaymentTx(final PaymentTx paymentTx) {
		paymentTx.setTxId(transactionIdCounter.incrementAndGet());
		paymentTransactions.put(paymentTx.getTxId(), paymentTx);
	}

	@Override
	public void clearTxs() {
		paymentTransactions.clear();
	}
}
