package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.PaymentsService;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class PaymentsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;
	@Autowired
	private PaymentsService paymentsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts and remove all payment transactions before each test.
		accountsService.getAccountsRepository().clearAccounts();
		paymentsService.getPaymentTxRepository().clearTxs();

		initAccounts();
	}

	@Test
	public void makePayment() throws Exception {
		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"senderId\":\"Id-123\",\"receiverId\":\"Id-124\",\"amount\":15.0}"))
				.andExpect(status().isCreated());

		assertAccount(accountsService.getAccount("Id-123"), "Id-123", new BigDecimal(85));
		assertAccount(accountsService.getAccount("Id-124"), "Id-124", new BigDecimal(115));
	}

	@Test
	public void makePaymentToYourself() throws Exception {
		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"senderId\":\"Id-123\",\"receiverId\":\"Id-123\",\"amount\":15.0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void makePaymentNoSenderId() throws Exception {
		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"receiverId\":\"Id-123\",\"amount\":15.0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void makePaymentNoReceiverId() throws Exception {
		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"senderId\":\"Id-123\",\"amount\":15.0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void makePaymentNoAmount() throws Exception {
		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"senderId\":\"Id-123\",\"receiverId\":\"Id-124\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void makePaymentNegativeAmount() throws Exception {
		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"senderId\":\"Id-123\",\"receiverId\":\"Id-124\",\"amount\":-15.0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void makePaymentsNotEnoughBalance() throws Exception {
		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"senderId\":\"Id-123\",\"receiverId\":\"Id-124\",\"amount\":50.0}"))
				.andExpect(status().isCreated());
		assertAccount(accountsService.getAccount("Id-123"), "Id-123", new BigDecimal(50));
		assertAccount(accountsService.getAccount("Id-124"), "Id-124", new BigDecimal(150));

		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"senderId\":\"Id-123\",\"receiverId\":\"Id-124\",\"amount\":50.0}"))
				.andExpect(status().isCreated());
		assertAccount(accountsService.getAccount("Id-123"), "Id-123", new BigDecimal(0));
		assertAccount(accountsService.getAccount("Id-124"), "Id-124", new BigDecimal(200));

		this.mockMvc.perform(post("/v1/payments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"senderId\":\"Id-123\",\"receiverId\":\"Id-124\",\"amount\":50.0}"))
				.andExpect(status().isBadRequest());
	}


	private void initAccounts(){
		accountsService.createAccount(new Account("Id-123", new BigDecimal(100)));
		accountsService.createAccount(new Account("Id-124", new BigDecimal(100)));
	}

	private void assertAccount(final Account account, final String id, final BigDecimal balance){
		assertThat(account.getAccountId()).isEqualTo(id);
		assertThat(account.getBalance()).isEqualByComparingTo(balance);
	}
}