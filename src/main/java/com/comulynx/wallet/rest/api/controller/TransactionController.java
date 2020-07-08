package com.comulynx.wallet.rest.api.controller;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comulynx.wallet.rest.api.AppUtilities;
import com.comulynx.wallet.rest.api.exception.ResourceNotFoundException;
import com.comulynx.wallet.rest.api.model.Account;
import com.comulynx.wallet.rest.api.model.Transaction;
import com.comulynx.wallet.rest.api.repository.AccountRepository;
import com.comulynx.wallet.rest.api.repository.TransactionRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
	private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
	private Gson gson = new Gson();

	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private AccountRepository accountRepository;
	@GetMapping("/")
	public List<Transaction> getAllTransaction() {
		return transactionRepository.findAll();
	}


	/**
	 * Get Last 100 Transactions By CustomerId

	 * 
	 * @param request
	 * @return
	 * @throws ResourceNotFoundException
	 */
	@PostMapping(value = "/last-100-transactions", consumes = { MediaType.APPLICATION_JSON_UTF8_VALUE},produces = { MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getLast100TransactionsByCustomerId(@RequestBody String request)
			throws ResourceNotFoundException {
		try {
			final JsonObject req = gson.fromJson(request, JsonObject.class);
			String customerId = req.get("customerId").getAsString();

			// TODO : Add login here to get Last 100 Transactions By CustomerId
			List<Transaction> last100Transactions = null;

			return ResponseEntity.ok().body(gson.toJson(last100Transactions));
		} catch (Exception ex) {
			logger.info("Exception {}", AppUtilities.getExceptionStacktrace(ex));

			return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@PostMapping(value = "/send-money", consumes = { MediaType.APPLICATION_JSON_UTF8_VALUE},produces = { MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> doSendMoneyTransaction(@RequestBody String request) throws ResourceNotFoundException {
		try {
			
	        Random rand = new Random(); 

	        
			JsonObject response = new JsonObject();

			final JsonObject balanceRequest = gson.fromJson(request, JsonObject.class);
			String customerId = balanceRequest.get("customerId").getAsString();
			String accountFrom = balanceRequest.get("accountFrom").getAsString();
			String accountTo = balanceRequest.get("accountTo").getAsString();
			double amount = balanceRequest.get("amount").getAsDouble();

			Account senderAccount = accountRepository.findAccountByAccountNo(accountFrom)
					.orElseThrow(() -> new ResourceNotFoundException("Account " + accountFrom + " NOT found for this"));

			Account beneficiaryAccount = accountRepository.findAccountByAccountNo(accountTo)
					.orElseThrow(() -> new ResourceNotFoundException("Account " + accountTo + " NOT found for this"));

  

	        
			String debitTransactionId = "TRN"+rand.nextInt(100000)+1000;
			String creditTransactionId = "TRN"+rand.nextInt(100000)+1001;

			// Debit
			Transaction transactionDebit = new Transaction();
			transactionDebit.setTransactionId(debitTransactionId);
			transactionDebit.setCustomerId(customerId);
			transactionDebit.setAccountNo(senderAccount.getAccountNo());
			transactionDebit.setAmount(amount);
			transactionDebit.setBalance(senderAccount.getBalance() - amount);
			transactionDebit.setTransactionType("FT");
			transactionDebit.setDebitOrCredit("Debit");
			transactionRepository.save(transactionDebit);
			// Credit
			Transaction transactionCredit = new Transaction();
			transactionCredit.setTransactionId(creditTransactionId);
			transactionCredit.setCustomerId(beneficiaryAccount.getCustomerId());
			transactionCredit.setAccountNo(beneficiaryAccount.getAccountNo());
			transactionCredit.setAmount(amount);
			transactionCredit.setBalance(beneficiaryAccount.getBalance() + amount);
			transactionCredit.setTransactionType("FT");
			transactionCredit.setDebitOrCredit("Credit");
			transactionRepository.save(transactionCredit);

			response.addProperty("response_status", true);
			response.addProperty("response_message", "Transaction Successful");

//			return ResponseEntity.ok().body(gson.toJson(response));
			return ResponseEntity.status(200).body(gson.toJson(response));
		} catch (Exception ex) {
			logger.info("Exception {}", AppUtilities.getExceptionStacktrace(ex));

			return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	/**
	 * Should return last 5 transactions from the database
	 * 
	 * @param request
	 * @return
	 * @throws ResourceNotFoundException
	 */
	@PostMapping(value = "/mini-statement", consumes = { MediaType.APPLICATION_JSON_UTF8_VALUE},produces = { MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getMiniStatementByCustomerIdAndAccountNo(@RequestBody String request)
			throws ResourceNotFoundException {
		try {
			final JsonObject balanceRequest = gson.fromJson(request, JsonObject.class);
			String customerId = balanceRequest.get("customerId").getAsString();
			String accountNo = balanceRequest.get("accountNo").getAsString();

			// FIXME: Should return last 5 transactions from the database
			List<Transaction> miniStatement = transactionRepository
					.getMiniStatementUsingCustomerIdAndAccountNo(customerId, accountNo);

			return ResponseEntity.ok().body(gson.toJson(miniStatement));
		} catch (Exception ex) {
			logger.info("Exception {}", AppUtilities.getExceptionStacktrace(ex));

			return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

}
