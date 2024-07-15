import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import {
  StripePaymentElementComponent,
  injectStripe,
} from 'ngx-stripe';
import { environment } from '../../environment';
import { StripeElementsOptions, StripePaymentElementOptions } from '@stripe/stripe-js';
import { UserService } from '../../service/user.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-stripe',
  templateUrl: './stripe.component.html',
  styleUrl: './stripe.component.css'
})
// https://ngx-stripe.dev/docs/elements
export class StripeComponent implements OnInit {
  
  @ViewChild(StripePaymentElementComponent)
  paymentElement!: StripePaymentElementComponent;

  private readonly fb = inject(FormBuilder);
  private readonly userSvc = inject(UserService);
  private readonly router = inject(Router);
  paymentElementForm !: FormGroup;

  elementsOptions: StripeElementsOptions = {
    locale: 'en',
    appearance: {
      theme: 'stripe',
    },
  };

  paymentElementOptions: StripePaymentElementOptions = {
    layout: {
      type: 'tabs',
      defaultCollapsed: false,
      radios: false,
      spacedAccordionItems: false
    }
  };
 
  stripe = injectStripe(environment.stripe_publishable_key);
  paying = signal(false);

  ngOnInit() {
    this.paymentElementForm = this.fb.group({
      name: this.fb.control('Test User', [Validators.required]),
      email : this.fb.control("user@email.com", [Validators.required]),
      address : this.fb.control(""),
      zipcode : this.fb.control(""),
      city : this.fb.control(""), 
      amount: this.fb.control(50, [Validators.required])
    })

    this.userSvc
      .createPaymentIntent({
        amount: this.paymentElementForm.get('amount')?.value,
        currency: 'sgd'
      })
      .subscribe(pi => {
        this.elementsOptions.clientSecret = pi.client_secret as string;
        // console.log(pi.client_secret)
      });
  }

  pay() {
    // console.log(this.paymentElementForm.getError)
    // console.log("value:", this.paymentElementForm.getRawValue());
    // console.log(this.paying())
    // console.log( this.paymentElementForm.invalid)
    if (this.paying() || this.paymentElementForm.invalid) return;
    this.paying.set(true);

    const { name, email, address, zipcode, city } = this.paymentElementForm.getRawValue();
    // console.log("value:", this.paymentElementForm.getRawValue());
    this.stripe
      .confirmPayment({
        elements: this.paymentElement.elements,
        confirmParams: {
          payment_method_data: {
            billing_details: {
              name: name as string,
              email: email as string,
              address: {
                line1: address as string,
                postal_code: zipcode as string,
                city: city as string
              }
            }
          }
        },
        redirect: 'if_required'
      })
      .subscribe(result => {
        this.paying.set(false);
        // console.log('Result', result);
        if (result.error) {          
          alert(JSON.stringify({ success: false, error: result.error.message }));
        } else {          
          if (result.paymentIntent.status === 'succeeded') {            
            alert(name + ", thank you for your sponsorship");
            this.router.navigate(['/']);
          }
        }
      });
  }

}
