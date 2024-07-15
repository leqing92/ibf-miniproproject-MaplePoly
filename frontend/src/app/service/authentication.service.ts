import { Injectable, Signal, WritableSignal, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, firstValueFrom } from 'rxjs';
import { map } from 'rxjs/operators';
import { User, UserState } from '../models/user';
import { Store } from '@ngrx/store';
import * as UserAction from '../state/user.action';

@Injectable({
  providedIn: 'root'
})
export class AuthService {  

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly store = inject(Store);

  // to change to effect / signal
  private isAuthedSignal : WritableSignal<boolean> = signal(false);
  // public readonly hasAuthed: Signal<boolean> = this.isAuthedSignal.asReadonly(); 

  // to allow the auth still valid for frontend after refresh
  private tokenKey = 'authToken';
  private sevendays :number = 7 * 24 * 60 * 60 * 1000;

  signup(data : User): Observable<any>{
    return this.http.post<any>('/api/auth/signup', data, {responseType: 'text' as 'json'})
        // .pipe(
        //     map(response => {                      
        //     return response;
        //     })
        // );
  }

  login(data : {username: string, password: string}): Observable<any> {
    return this.http.post<any>('/api/auth/generateToken', data)
      .pipe(
        map(response => {          
          const expirationDate = new Date(new Date().getTime() + this.sevendays);
          localStorage.setItem(this.tokenKey, JSON.stringify({ token: this.tokenKey, expirationDate: expirationDate.toISOString() }));
          // console.log("response", response);
          localStorage.setItem("username", response.username);
          this.isAuthedSignal.set(true);

          return response;
        })
      );
  }

  reloadDatatoStore(): Promise<UserState>{
    return firstValueFrom(this.http.get<UserState>('/api/auth/user/refresh'))
      .then(response => {
          // console.log("response username:", response.username);
          this.store.dispatch(UserAction.setUser({username: response.username, email: response.email, gid: response.gid, inGame: response.inGame}));
          return response;
        })
      .catch(error =>{  
          alert("error" + JSON.stringify(error))
          return error;
      })
  }

  logout(): Observable<any> {
    return this.http.post('/api/auth/logout', {}, {responseType: 'text' as 'json'})
        .pipe(
            map(resp =>{
                this.isAuthedSignal.set(false);
                this.store.dispatch(UserAction.clearUser());
                localStorage.removeItem(this.tokenKey);
                localStorage.removeItem("username");

                return resp;
            })
        );
  }

  get isAuthed(): WritableSignal<boolean> {
    if(localStorage.getItem(this.tokenKey)){
      this.isAuthedSignal.set(true);
      this.reloadDatatoStore();
    }
    return this.isAuthedSignal;
  }

}
