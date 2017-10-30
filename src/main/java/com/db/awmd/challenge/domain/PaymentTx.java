package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class PaymentTx {

	private Integer txId;

	@NotNull
	@NotEmpty
	private String senderId;

	@NotNull
	@NotEmpty
	private String receiverId;

	@NotNull
	@Min(value = 0, message = "Transfer amount must be positive.")
	private BigDecimal amount;


	@JsonCreator
	public PaymentTx(final @JsonProperty("sender") String sender,
					 final @JsonProperty("receiver") String receiver,
					 final @JsonProperty("amount") BigDecimal amount) {
		this.senderId = sender;
		this.receiverId = receiver;
		this.amount = amount;
		this.txId = null;
	}
}
