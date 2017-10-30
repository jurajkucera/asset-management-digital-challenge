package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.PaymentTx;
import com.db.awmd.challenge.service.PaymentsService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@Slf4j
public class PaymentsController {

	private final PaymentsService paymentsService;

	@Autowired
	public PaymentsController(final PaymentsService paymentsService) {
		this.paymentsService = paymentsService;
	}


	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> makePayment(@RequestBody @Valid PaymentTx paymentTx) {
		log.info("Making a payment {}", paymentTx);

		try {
			this.paymentsService.transfer(paymentTx);
		} catch (RuntimeException ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}
}
