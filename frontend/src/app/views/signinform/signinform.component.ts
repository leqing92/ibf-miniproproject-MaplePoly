import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../service/authentication.service';
import { UserService } from '../../service/user.service';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import * as UserAction from '../../state/user.action';
import { HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environment';

@Component({
  selector: 'app-signinform',
  templateUrl: './signinform.component.html',
  styleUrl: './signinform.component.css'
})
export class SigninformComponent {
  
  private readonly fb = inject(FormBuilder);
  private readonly authSvc = inject(AuthService);
  private readonly httpSvc = inject(UserService);
  private readonly router = inject(Router);
  // private readonly store = inject(Store);

  form !: FormGroup;
  captcha !: string | null;
  SITEKEY = environment.google_recaptcha_site_key;
  hidePassword: boolean = true;
  
  ngOnInit(): void {
    this.form = this.fb.group({
      email : this.fb.control("", [Validators.required, Validators.email]),
      password : this.fb.control("", [Validators.required]),
      recaptcha: this.fb.control('', [Validators.required])
    })
  }

  login(): void {    
    this.authSvc.login(this.form.value).subscribe({
      next: response => {
        // console.log("response username:", response.username);
        // this.store.dispatch(UserAction.setUser({username: response.username, email: response.email, gid: response.gid, inGame: response.inGame}));
        this.router.navigate(['/gamecenter']);
      },
      error: (error : HttpErrorResponse) => {
        alert(error.error.error)
        const recaptchaValue = this.form.get('recaptcha')?.value;
        this.form.reset(); //this will clear recaptcha field also so must patch back
        this.form.patchValue({ recaptcha: recaptchaValue });
      },
      complete: () => {
        console.log('Login request completed');
      }
    });
  }

  logout() {
    this.authSvc.logout().subscribe(
      response => {
        // this.store.dispatch(UserAction.clearUser());
        console.log(response);
      },
      error => {        
        console.error('Logout failed:', error);
        alert(JSON.stringify(error));
      }    
    );
  }  

  toWelcome() {
    this.httpSvc.toWelcome()
      .then(
        resp => console.log("toWelcome:", resp)
      )
      .catch(
        err => console.log("toWelcome err:", err)
      )
  }

  toUserProfile() {
    this.httpSvc.toUserProfile()
      .then(
        resp => console.log("toUserProfile:", resp)
      )
      .catch(
        err => console.log("toUserProfile err:", err)
      )
  }

  toAdminProfile() {
   this.httpSvc.toAdminProfile()
    .then(
      resp => console.log("toAdminProfile:", resp)
    )
    .catch(
      err => console.log("toAdminProfile err:", err)
    )
  }

  resolved(captchaResponse: string | null) {
    this.captcha = captchaResponse;
    this.form.patchValue({ recaptcha: captchaResponse });  // update form control value so can sign in
    // console.log('resolved captcha response: ', this.captcha);
  }

}
