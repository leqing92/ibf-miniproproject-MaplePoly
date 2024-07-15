package project.monopoly.security.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.json.Json;
import project.monopoly.security.entity.CheckoutPayment;

// https://docs.stripe.com/payment-links/api
// https://docs.stripe.com/checkout/quickstart
// https://dashboard.stripe.com/test/payments - to check payment pass through or not
@RestController
@RequestMapping(path="/api/stripe")
public class StripeController {

    @Value("${stripe.key}")
    String stripeKey;

    @Value("${stripe.secret.key}")
    String stripeSecretKey;

    // @Value("${mydomain}")
    // String domain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // https://docs.stripe.com/api/payment_intents/object - create payment intent 
    @PostMapping(path="/secretkey")
    public ResponseEntity<String> createPaymentIntent(@RequestBody CheckoutPayment entity) throws StripeException {

        Stripe.apiKey = stripeSecretKey;
        
        PaymentIntentCreateParams params = 
            PaymentIntentCreateParams.builder()
                .setAmount(entity.getAmount())
                .setCurrency(entity.getCurrency())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return ResponseEntity.status(200).body(
                    Json.createObjectBuilder()
                        .add("client_secret", paymentIntent.getClientSecret())                        
                        .build().toString()
                );
    }

    // // doesnt work - using frontend to post to stripe
    // // to find out how to get and set the payment detail from frontend
    // @PostMapping(path="/pay")
    // public ResponseEntity<String> postPayment(@RequestBody CheckoutPayment entity) {
    //     System.out.println(entity.toString());
    //     Stripe.apiKey = stripeKey;
    //     try {
    //         PaymentIntent paymentIntent = PaymentIntent.retrieve("pi_3MtweELkdIwHu7ix0Dt0gF2H");
    //         PaymentIntentConfirmParams params =
    //             PaymentIntentConfirmParams.builder()
    //                 .setPaymentMethod("pm_card_visa")
    //                 .setReturnUrl("https://www.example.com")
    //                 .build();
            
            
    //         PaymentIntent confirmedPaymentIntent = paymentIntent.confirm(params);

    //         if(confirmedPaymentIntent.getStatus().equals("succeeded")){
    //             return ResponseEntity.status(200).body(
    //                     Json.createObjectBuilder()
    //                         .add("client_secret", confirmedPaymentIntent.getClientSecret())                        
    //                         .build().toString()
    //                 );
    //         }
    //     } catch (StripeException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }

    //     return ResponseEntity.status(500).body(
    //                 Json.createObjectBuilder()
    //                     .add("error", "internal server error")                        
    //                     .build().toString()
    //             );
    // }
    

    // @PostMapping("/post")
    // public String paymentWithCheckoutPage(@RequestBody CheckoutPayment payment) throws StripeException, JsonProcessingException {        
        
    //     Stripe.apiKey = stripeSecretKey;
    //     Gson gson = new Gson();

    //     // SessionCreateParams params = SessionCreateParams.builder()
    //     //     .setMode(SessionCreateParams.Mode.PAYMENT)
    //     //     .setSuccessUrl( "http://localhost:4200/signin")
    //     //     .setCancelUrl("http://localhost:4200/signup")
    //     //     .setAutomaticTax(
    //     //         SessionCreateParams.AutomaticTax.builder()
    //     //         .setEnabled(true)
    //     //         .build())
    //     //         .addLineItem(
    //     //             SessionCreateParams.LineItem.builder()
    //     //               .setQuantity(1L)
    //     //               .setPriceData(
    //     //                 SessionCreateParams.LineItem.PriceData.builder()
    //     //                   .setCurrency("sgd")
    //     //                   .setUnitAmount(1000L)
    //     //                   .setProductData(
    //     //                     SessionCreateParams.LineItem.PriceData.ProductData.builder()
    //     //                       .setName("sponsorship")
    //     //                       .build())
    //     //                   .build())
    //     //               .build())
    //     //             .build();
      
    //     // create a stripe session
	// 	// Session session = Session.create(params);
    //     Map<String, String> map = new HashMap<>();
    //     // System.out.println(session.toJson());
    //     // map.put("client_secret", session.getRawJsonObject().getAsJsonPrimitive("id").getAsString() +"_secret_"+ stripeSecretKey);

    //     // amount in cent
    //     PaymentIntentCreateParams params = 
    //         PaymentIntentCreateParams.builder()
    //             .setAmount(50L)
    //             .setCurrency("sgd")
    //             .build();

    //     PaymentIntent paymentIntent = PaymentIntent.create(params);
    //     map.put("client_secret", paymentIntent.getClientSecret());
    //     System.out.println(paymentIntent.toJson());
    //     // We can return only the sessionId as a String
	// 	return objectMapper.writeValueAsString(map);
    // }
    
}
