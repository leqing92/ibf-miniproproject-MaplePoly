import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, firstValueFrom } from "rxjs";

Injectable()

export class UserService{
   
    private readonly http = inject(HttpClient);
//for security testing
    toWelcome(): Promise<any> {
        return firstValueFrom(this.http.get<any>('/api/auth/welcome', { withCredentials: false ,  responseType: 'text' as 'json'}, ))
    }

    toUserProfile(): Promise<any> {
        return firstValueFrom(this.http.get<any>('/api/auth/user/userProfile', { withCredentials: true ,  responseType: 'text' as 'json' }));
    }

    toAdminProfile(): Promise<any> {
        return firstValueFrom(this.http.get<any>('/api/auth/admin/adminProfile', { withCredentials: true ,  responseType: 'text' as 'json' }));
    }
//for stripe
    createPaymentIntent(arg0: { amount: number ; currency: string; }) : Observable<{client_secret : string}>{
        return this.http.post<{client_secret : string}>('/api/stripe/secretkey', arg0);
    }

    makepayment(arg0: { amount: number ; currency: string; }) : Observable<{client_secret : string}>{
        return this.http.post<{client_secret : string}>('/api/stripe/post', arg0);
    }

}