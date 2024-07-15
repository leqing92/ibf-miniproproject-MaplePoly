import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { AuthService } from "./authentication.service";
import { Observable, catchError, throwError } from "rxjs";
import { Router } from "@angular/router";

@Injectable()
export class AuthInterceptor implements HttpInterceptor{
    private readonly authSvc = inject(AuthService);
    private readonly router = inject(Router);
    
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(
            catchError((err : HttpErrorResponse) =>{
                // console.error(err);
                if(err.status === 401){
                    this.authSvc.logout().subscribe({
                        next: (resp) => {
                            alert("Your login session expired.\n Please log in again.");
                            this.router.navigate(['signin'])
                        }
                    });
                }
                return throwError(() => err);
            })
        );
    }
}
// || err.status === 403