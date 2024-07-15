import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from './views/main/main.component';
import { SignupformComponent } from './views/signupform/signupform.component';
import { SigninformComponent } from './views/signinform/signinform.component';
import { authGuard } from './guards/AuthGuard';
import { GameCenterComponent } from './views/game-center/game-center.component';
import { GameRoomComponent } from './views/game-room/game-room.component';
import { GameComponent } from './views/game/game.component';
import { StripeComponent } from './views/stripe/stripe.component';
import { LeaderboardComponent } from './views/leaderboard/leaderboard.component';

const routes: Routes = [
  { path: '', component: MainComponent },
  { path: 'signup', component: SignupformComponent },
  { path: 'signin', component: SigninformComponent },
  { path: 'gamecenter', component: GameCenterComponent , canActivate: [authGuard]},
  { path: 'gameroom/:id', component: GameRoomComponent , canActivate: [authGuard]},
  { path: 'game/:id', component: GameComponent , canActivate: [authGuard]},
  { path: 'game/:id/leaderboard', component: LeaderboardComponent, canActivate: [authGuard]},
  { path: 'sponsor', component: StripeComponent },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
