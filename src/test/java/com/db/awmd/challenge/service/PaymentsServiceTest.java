package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.PaymentTx;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.SenderCannotBeTheReceiverException;
import com.db.awmd.challenge.repository.PaymentTxRepository;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PaymentsServiceTest {

	@InjectMocks
	private PaymentsService paymentsService;

	@Mock
	private AccountsService accountsService;
	@Mock
	private PaymentTxRepository paymentTxRepository;
	@Mock
	private NotificationService notificationService;

	private Account account123;
	private Account account124;

	@Before
	public void setUp() {
		account123 = new Account("ID-123", new BigDecimal(100));
		account124 = new Account("ID-124", new BigDecimal(100));

		when(accountsService.getAccount("ID-123")).thenReturn(account123);
		when(accountsService.getAccount("ID-124")).thenReturn(account124);
	}

	@Test
	public void makePayment() throws Exception {
		final PaymentTx paymentTx = new PaymentTx("ID-123", "ID-124", new BigDecimal(15));

		paymentsService.transfer(paymentTx);

		verify(paymentTxRepository).createPaymentTx(paymentTx);
		verify(notificationService).notifyAboutTransfer(account123, "Sent 15 to ID-124");
		verify(notificationService).notifyAboutTransfer(account124, "Received 15 from ID-123");
	}

	@Test(expected = SenderCannotBeTheReceiverException.class)
	public void makePaymentToYourself() throws Exception {
		final PaymentTx paymentTx = new PaymentTx("ID-123", "ID-123", new BigDecimal(15));

		paymentsService.transfer(paymentTx);

		verify(paymentTxRepository, never()).createPaymentTx(paymentTx);
		verify(notificationService, never()).notifyAboutTransfer(any(),anyString());
	}

	@Test(expected = InsufficientFundsException.class)
	public void makePaymentAboveBalance() throws Exception {
		final PaymentTx paymentTx = new PaymentTx("ID-123", "ID-124", new BigDecimal(150));

		paymentsService.transfer(paymentTx);

		verify(paymentTxRepository, never()).createPaymentTx(paymentTx);
		verify(notificationService, never()).notifyAboutTransfer(any(),anyString());
	}



}