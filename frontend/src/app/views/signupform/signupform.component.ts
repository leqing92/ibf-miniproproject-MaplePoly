import { Component, OnInit, inject } from '@angular/core';
import { User } from '../../models/user';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CustomValidator } from '../../validator/custom-validator';
import { UserService } from '../../service/user.service';
import { AuthService } from '../../service/authentication.service';
import { Router } from '@angular/router';
import { environment } from '../../environment';

@Component({
  selector: 'app-signupform',
  templateUrl: './signupform.component.html',
  styleUrl: './signupform.component.css'
})
export class SignupformComponent implements OnInit{ 
  
  private readonly fb = inject(FormBuilder);
  private readonly authSvc = inject(AuthService);
  private readonly userSvc = inject(UserService);
  private readonly router = inject(Router);

  signUpForm !: FormGroup;
  hidePassword: boolean = true;
  hideConfirmPassword: boolean = true;
  //set date picker
  minDate = new Date(1900, 1, 1);
  maxDate = new Date();  
  captcha !: string | null;
  SITEKEY = environment.google_recaptcha_site_key;
  
  ngOnInit(): void {

    this.signUpForm = this.fb.group({
      name : this.fb.control<string>("", [Validators.required, Validators.minLength(5), Validators.maxLength(50)]),
      password : this.fb.control<string>("", [Validators.required]),
      confirmPassword : this.fb.control<string>("", [Validators.required]),
      email : this.fb.control<string>("", [Validators.required, Validators.email]),
      dob : this.fb.control<Date | null>(null, [Validators.required, CustomValidator.notFuture]),
      recaptcha: this.fb.control('', [Validators.required])  
    },
    {
      validators: CustomValidator.confirmPassword('password', 'confirmPassword')
    })    
  }

  onSubmit(){
    const input : User = this.signUpForm.value as User;

    this.authSvc.signup(input)
      .subscribe(
        resp =>{
          // console.info(resp);
          alert(resp)
          this.router.navigate(['/signin']);
        },
        error=>{
          alert(JSON.stringify(error.error))
          if(error.error === 'Email is already in use.')
            this.clear("email")
          if(error.error === 'Username is already in use.')
            this.clear("name")
        }
    )
    
  } 

  clear(value : string) {
    this.signUpForm.get(value)?.reset();
  }  
  
  resolved(captchaResponse: string | null) {
    this.captcha = captchaResponse;
    this.signUpForm.patchValue({ recaptcha: captchaResponse });  // update form control value so can sign in
    // console.log('resolved captcha response: ', this.captcha);
  }
}
