import { NgModule, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BoardComponent } from './components/board/board.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MaterialModule } from './materials/material.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NavbarComponent } from './components/navbar/navbar.component';
import { MainComponent } from './views/main/main.component';
import { TileDetailComponent } from './components/dialog/tile-detail.component';
import { SignupformComponent } from './views/signupform/signupform.component';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import {provideNativeDateAdapter} from '@angular/material/core';
import { SigninformComponent } from './views/signinform/signinform.component';
import { AuthService } from './service/authentication.service';
import { UserService } from './service/user.service';
import { RecaptchaModule } from 'ng-recaptcha';
import { GameCenterComponent } from './views/game-center/game-center.component';
import { CharacterComponent } from './components/character/character.component';
import { GameService } from './service/game.service';
import { GameRoomComponent } from './views/game-room/game-room.component';
import { GameCenterService } from './service/game-center.service';
import { WebSocketService } from './service/websocket.service';
import { GameComponent } from './views/game/game.component';
import { GameFunctionComponent } from './components/game-function/game-function.component';
import { StoreModule } from '@ngrx/store';
import { userReducer } from '../app/state/user.reducer'
import { playerReducer, propertyReducer } from './state/game.reducer';
import { StripeComponent } from './views/stripe/stripe.component';
import { NgxStripeModule } from 'ngx-stripe';
import { environment } from './environment';
import { GiphyBottomSheetComponent } from './components/giphy-bottom-sheet/giphy-bottom-sheet.component';
import { AuthInterceptor } from './service/auth.interceptor';
import { ChartComponent } from './components/chart/chart.component';
import { ServiceWorkerModule } from '@angular/service-worker';
import { TradeComponent } from './components/trade/trade.component';
import { LeaderboardComponent } from './views/leaderboard/leaderboard.component';

@NgModule({
  declarations: [
    AppComponent,
    BoardComponent,
    CharacterComponent,
    NavbarComponent,
    MainComponent,
    TileDetailComponent,
    SignupformComponent,
    SigninformComponent,
    GameCenterComponent,
    GameRoomComponent,
    GameComponent,
    GameFunctionComponent,
    StripeComponent,
    GiphyBottomSheetComponent,
    ChartComponent,
    TradeComponent,
    LeaderboardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    RecaptchaModule, // npm i ng-recaptcha
    StoreModule.forRoot({
      user: userReducer, 
      property : propertyReducer,
      player: playerReducer
    }), // for NgRx store, playerstate still in-progress to add effect
    NgxStripeModule.forRoot(environment.stripe_publishable_key),    
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    })

  ],
  providers: [
    provideAnimationsAsync(),
    provideNativeDateAdapter(), //for mat-date
    AuthService,
    UserService,
    GameService,
    GameCenterService,
    WebSocketService,
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
