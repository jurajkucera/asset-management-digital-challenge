package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.PaymentTx;

public interface PaymentTxRepository {

	void createPaymentTx(final PaymentTx paymentTx);

	void clearTxs();
}
