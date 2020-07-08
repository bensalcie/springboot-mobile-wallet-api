package com.comulynx.wallet.rest.api.controller;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comulynx.wallet.rest.api.AppUtilities;
import com.comulynx.wallet.rest.api.model.Account;
import com.comulynx.wallet.rest.api.model.Customer;
import com.comulynx.wallet.rest.api.repository.AccountRepository;
import com.comulynx.wallet.rest.api.repository.CustomerRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
	private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

	private Gson gson = new Gson();

	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private AccountRepository accountRepository;
	@GetMapping("/")
	public List<Customer> getAllCustomers() {
		return customerRepository.findAll();
	}

	/**
	 * Fix Customer Login functionality
	 * 
	 * Login
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/login")
	public ResponseEntity<?> customerLogin(@RequestBody String request) {
		try {
			JsonObject response = new JsonObject();

			final JsonObject req = gson.fromJson(request, JsonObject.class);
			String customerId = req.get("customerId").getAsString();
			String customerPIN = req.get("pin").getAsString();

			// TODO : Add Customer login logic here. Login using customerId and
			// PIN
			// NB: We are using plain text password for testing Customer login
			// If customerId doesn't exists throw an error "Customer does not exist"
			// If password do not match throw an error "Invalid credentials"

			Optional<Customer>  incomingCustomer = customerRepository.findByCustomerId(customerId);
			if(incomingCustomer.isPresent()){
				if(incomingCustomer.get().getPin().equals(customerPIN)){
					//we have the customer
					//TODO : Return a JSON object with the following after successful login
					//Customer Name, Customer ID, email and Customer Account
					Customer customer = incomingCustomer.get();
					JsonObject jsonObject=new JsonObject();
					jsonObject.addProperty("Customer Name",customer.getFirstName()+" "+customer.getLastName());
					jsonObject.addProperty("Customer ID",customer.getId());
					jsonObject.addProperty("Customer Email",customer.getEmail());
					jsonObject.addProperty("Customer Account",accountRepository.findAccountByCustomerId(customer.getId()).get().getAccountNo());


					return ResponseEntity.status(200).body(jsonObject);

				}else{

					return ResponseEntity.status(200).body("wrong password");
				}

			}else{

				return ResponseEntity.status(200).body("customer does not exist");
			}


		} catch (Exception ex) {
			logger.info("Exception {}", AppUtilities.getExceptionStacktrace(ex));
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	/**
	 *  Add required logic
	 *  
	 *  Create Customer
	 *  
	 * @param customer
	 * @return
	 */
	@PostMapping("/")
	public ResponseEntity<?> createCustomer(@Valid @RequestBody Customer customer) {
		try {
			String customerPIN = customer.getPin();
			String email = customer.getEmail();
			long customerId = customer.getId();

			int hashedPIN = customerPIN.hashCode();


			//  : Add logic to check if Customer with provided email, or
			// customerId exists. If exists, throw a Customer with [?] exists
			// Exception.

			Optional<Customer> newCustomer  = customerRepository.findByCustomerId(String.valueOf(customerId));

			if(newCustomer.isPresent()){
				//id already exist
				return ResponseEntity.status(500).body("customer already exists");
			}

			for(Customer customer1 : customerRepository.findAll()){
				if(customer1.getEmail().equals(email)){
					//email exist
					return ResponseEntity.status(200).body("customer already exists");

				}else{
					//email does not exist
//					ResponseEntity.status(500).body("customer does not exist");

					String accountNo = generateAccountNo(customer.getCustomerId());
					Account account = new Account();
					account.setCustomerId(customer.getCustomerId());
					account.setAccountNo(accountNo);
					account.setBalance(0.0);
					accountRepository.save(account);

				}
			}



			return ResponseEntity.ok().body(customerRepository.save(customer));
		} catch (Exception ex) {
			logger.info("Exception {}", AppUtilities.getExceptionStacktrace(ex));

			return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	/**
	 *  Add required functionality
	 *  
	 * generate a random but unique Account No (NB: Account No should be unique
	 * in your accounts table)
	 * 
	 */
	private String generateAccountNo(String customerId) {
		// TODO : Add logic here - generate a random but unique Account No (NB:
		// Account No should be unique in the accounts table)

		String start = customerId;
		Random value = new Random();
		int r1 = value.nextInt(10);
		int r2 = value.nextInt(10);
		start += Integer.toString(r1) + Integer.toString(r2) + " ";

		int count = 0;
		int n = 0;
		for(int i =0; i < 12;i++)
		{
			if(count == 4)
			{
				start += " ";
				count =0;
			}
			else
				n = value.nextInt(10);
			start += Integer.toString(n);
			count++;

		}


		return start;
	}
}
