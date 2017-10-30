package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.PaymentTx;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.SenderCannotBeTheReceiverException;
import com.db.awmd.challenge.repository.PaymentTxRepository;
import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentsService {

	private static final String SENDER_NOTIFICATION_MESSAGE = "Sent %s to %s";
	private static final String SENDER_IS_THE_RECEIVER_MESSAGE = "Sender %s equals to receiver %s";
	private static final String RECEIVER_NOTIFICATION_MESSAGE = "Received %s from %s";
	private static final String NOT_ENOUGH_BALANCE = "Not enough balance for %s to send %s";

	@Getter
	private final NotificationService notificationService;

	@Getter
	private final AccountsService accountsService;

	@Getter
	private final PaymentTxRepository paymentTxRepository;

	@Autowired
	public PaymentsService(final NotificationService notificationService, final AccountsService accountsService,
						   final PaymentTxRepository paymentTxRepository) {
		this.notificationService = notificationService;
		this.accountsService = accountsService;
		this.paymentTxRepository = paymentTxRepository;
	}

	public void transfer(final PaymentTx paymentTx) {
		final Account senderAccount;
		final Account receiverAccount;

		if (paymentTx.getSenderId().equals(paymentTx.getReceiverId())){
			throw new SenderCannotBeTheReceiverException(String.format(SENDER_IS_THE_RECEIVER_MESSAGE,
					paymentTx.getSenderId(), paymentTx.getReceiverId()));
		}

		final ReentrantLock lock = new ReentrantLock();
		lock.lock();
		try {
			senderAccount = accountsService.getAccount(paymentTx.getSenderId());
			receiverAccount = accountsService.getAccount(paymentTx.getReceiverId());

			if (senderAccount.getBalance().doubleValue() < paymentTx.getAmount().doubleValue()){
				throw new InsufficientFundsException(String.format(NOT_ENOUGH_BALANCE,
						paymentTx.getSenderId(), paymentTx.getAmount().toPlainString()));
			}

			updateBalances(senderAccount, receiverAccount, paymentTx.getAmount());
		}
		finally {
			lock.unlock();
		}

		paymentTxRepository.createPaymentTx(paymentTx);
		notificationService.notifyAboutTransfer(senderAccount, String.format(SENDER_NOTIFICATION_MESSAGE,
				paymentTx.getAmount().toPlainString(), receiverAccount.getAccountId()));
		notificationService.notifyAboutTransfer(receiverAccount, String.format(RECEIVER_NOTIFICATION_MESSAGE,
				paymentTx.getAmount().toPlainString(), senderAccount.getAccountId()));
	}

	private void updateBalances(final Account transferFromAccount, final Account transferToAccount, final BigDecimal amount) {
		transferFromAccount.setBalance(transferFromAccount.getBalance().subtract(amount));
		transferToAccount.setBalance(transferToAccount.getBalance().add(amount));
	}

}
