import { Component, OnInit, inject } from '@angular/core';
import { AuthService } from '../../service/authentication.service';
import { Router } from '@angular/router';
import { UserState } from '../../models/user';
import { Store } from '@ngrx/store';
import { selectUser } from '../../state/user.selector';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit{
  
  private readonly authSvc = inject(AuthService); 
  private readonly router = inject(Router); 
  private readonly store = inject(Store);

  isAuthenticated !: boolean;
  userState !: UserState;
  inGame : boolean = false;
  gid !: string;
  username !: string;

  ngOnInit(): void {
    // console.log("before init auth", this.isAuthenticated);
    this.isAuthenticated = this.authSvc.isAuthed();
    // console.log("init auth", this.isAuthenticated);
    if(this.isAuthenticated){
      this.store.select(selectUser).subscribe({
        next : resp => {
          // this.userState = resp;
          // else if userState is null/undefined then html will throw error
          this.inGame = resp.inGame; 
          this.gid = resp.gid;
          this.username = resp.username;
          // console.log("inGame", this.inGame);
          // console.log("gid", this.gid);
          // console.log("username", this.username);
        }
      });
    }
  }

  // still sometime error sometimes no
  logout():void {
    if (confirm("Are you sure you want to logout?")) {
      if (this.isGameRoom()) {          
        this.router.navigate(['signin']).then(() => {
          this.authSvc.logout().subscribe({
            next: response => {
              window.location.reload(); //reload to clear the logout input
            },
            error: error => {
              console.error('Logout failed:', error);
            }
          });
        });
      } 
      else {          
        this.authSvc.logout().subscribe({
          next: response => {
            // console.log("logout success", response);
            this.isAuthenticated = false;
            this.router.navigate(['signin']);
          },
          error: error => {
            console.error('Logout failed:', error);
          }
        });
      }
    }
  }

  isSponsorComponent(): boolean {
    return this.router.url.includes('sponsor'); 
  }

  isMainComponent(): boolean {
    return this.router.url === '/';
  }

  isGameRoom(): boolean {
    return this.router.url.includes('gameroom');
  }
}
