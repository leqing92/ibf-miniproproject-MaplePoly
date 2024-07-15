import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate, CanDeactivateFn, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../service/authentication.service';
import { Router } from '@angular/router';
import { GameRoomComponent } from '../views/game-room/game-room.component';

export const authGuard: CanActivateFn = 
  ( route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Promise<boolean> | Observable<boolean> => {
      const authSvc = inject(AuthService);
      const router = inject(Router);

      if(!authSvc.isAuthed()){
          alert('Please login first');
          router.navigate(['/signin']);
      }
      return authSvc.isAuthed();
      // return authSvc.isAuthenticated().pipe(
      //   map(isAuthenticated => {
      //     if (!isAuthenticated) {
      //       alert('Please login first');
      //       router.navigate(['/signin']); 
      //     }
      //     return isAuthenticated;
      //   })
      // );
    };

// export const leaveGameRoom : CanDeactivateFn<GameRoomComponent> =
//   (comp: GameRoomComponent, route : ActivatedRouteSnapshot, state : RouterStateSnapshot) 
//   : boolean | UrlTree | Promise<boolean | UrlTree> | Observable<boolean | UrlTree> => {
//     var isLeave = false;
//     try {
//       if (!comp.isOwner) {
//         console.log("player");
//         comp.quitGame();
//       } 
//       else {
//         console.log("owner");
//         comp.deleteGame();
//       }
//       isLeave=true;
//       isLeave;
//     } 
//     catch (err) {
//       console.error('Error while quitting or deleting game:', err);
//       isLeave;
//     }
//     if(isLeave){
//       return true
//     }
//     return false;
//   }