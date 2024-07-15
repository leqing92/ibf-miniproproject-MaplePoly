import { Component, OnInit, inject } from '@angular/core';
import { AuthService } from './service/authentication.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit{
  title = 'monopoly-angular-material';
  
  // private readonly authSvc = inject(AuthService);

  ngOnInit(): void {
    // this.authSvc.checkAuthenticationStatus();
  }
}
