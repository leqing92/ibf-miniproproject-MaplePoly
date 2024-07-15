package project.monopoly.security.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPayment {

    // the payment intent
	private String id;	
	private String currency;	
	private long amount;

}