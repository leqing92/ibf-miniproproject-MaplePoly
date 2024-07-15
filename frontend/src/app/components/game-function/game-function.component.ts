import { Component, Input, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { WebSocketService } from '../../service/websocket.service';
import { Store } from '@ngrx/store';
import { selectAllPropertyStates, selectPlayerStateByName } from '../../state/game.selector';
import { Subject, combineLatest, takeUntil } from 'rxjs';
import { GameAction, PlayerState, PropertyState } from '../../models/gamestate';
import { Property } from '../../models/property';
import { MatDialog } from '@angular/material/dialog';
import { ChartComponent } from '../chart/chart.component';
import { TradeComponent } from '../trade/trade.component';

@Component({
  selector: 'app-game-function',
  templateUrl: './game-function.component.html',
  styleUrl: './game-function.component.css'
})
export class GameFunctionComponent implements OnInit, OnDestroy{
  
  private readonly webSocketSvc = inject(WebSocketService);
  private readonly store = inject(Store);
  
  isMenuOpen = true;
  @Input() move !: number;
  @Input() player !: string;
  @Input() gid !: string;
  @Input() isMoving !: boolean;
  @Input() properties !: Property[];
  
  private destroy$ = new Subject<void>();
  playerState ?: PlayerState;
  propertyStates ?: PropertyState[];
  // from gamestate
  canBuy : boolean = false;
  canBuild: boolean = false;
  hasMoved !:boolean;
  endTurn : boolean = false;
  hasBankrupted : boolean = false;

  ngOnInit(): void {
    this.subscribeToGameState();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.webSocketSvc.disconnect();
  }

  subscribeToGameState() : void {
    const playerState$ = this.store.select(selectPlayerStateByName(this.player));
    const propertyStates$ = this.store.select(selectAllPropertyStates);

    combineLatest([playerState$, propertyStates$])
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ([playerState, propertyStates]) => {
          this.playerState = playerState;
          this.propertyStates = propertyStates;
          this.updateCanBuy();
          this.updateCanBuild();
          this.updateControl();
        },
        error: error => {
          alert(error);
        },
        complete: () => {          
        }
      });
  }

  updateCanBuy() : void {
    if (this.playerState && this.propertyStates) {      
      const propertyState = this.propertyStates[this.playerState.position];
      this.canBuy = propertyState ? propertyState.canBuy : false;     
    } 
    else {
      this.canBuy = false;
    }
  }
  
  updateCanBuild() : void {
    if (this.playerState && this.propertyStates) {
      const property = this.propertyStates[this.playerState.position];
      if(property.owner !== this.playerState.name || property.star == 4 || property.id == 5
        || property.id == 15 || property.id == 25 || property.id == 35 || property.id == 12
        || property.id == 28
      ){
        this.canBuild = false;
      }
      else {
        this.canBuild = true;
      }
    } else {
      this.canBuild = false;
    }
  }

  updateControl() : void {
    if(this.playerState){
      this.hasMoved = this.playerState.hasMoved;
      this.endTurn = this.playerState.endTurn;
      this.hasBankrupted = this.playerState.hasBankrupted;
    }
  }

  toggleMenu() : void {
    this.isMenuOpen = !this.isMenuOpen;
  }
  
  rollDice() : void {
    const gameAction : GameAction = {
      action: 'rollDice',
      player: this.player,
      gid: this.gid,
      content: '',
      timestamp: new Date().toISOString()
    };
    this.webSocketSvc.sendMessage(gameAction);
  }

  buyProperty() : void {
    if (this.playerState) {
      const property = this.properties[this.playerState.position];
      if(this.playerState.money >= property.cost){
        const id = this.playerState.position.toString();
        const gameAction: GameAction = {
          action: 'buyProperty',
          player: this.player,
          gid: this.gid,
          content: id,
          timestamp: new Date().toISOString()
        };
        this.webSocketSvc.sendMessage(gameAction);
      }
      else{
        alert("Money insufficient")
      }
    } else {
      console.error('playerState is undefined');
    }   
  }
  
  build() : void {
    if (this.playerState) {
      const id = this.playerState.position;
      const property = this.properties?.at(id);
  
      if (property) {
        if (this.playerState.money > property.build) {
          const gameAction: GameAction = {
            action: 'build',
            player: this.player,
            gid: this.gid,
            content: id.toString(),
            timestamp: new Date().toISOString()
          };
          this.webSocketSvc.sendMessage(gameAction);
        } else {
          alert("Money insufficient");
        }
      } else {
        alert("Property not found.\n Please reload the page");
      }
    } else {
      alert("Player state not found.\n Please reload the page");
    }
  }

  completeTurn() : void {
    const gameAction: GameAction = {
      action: 'endTurn',
      player: this.player,
      gid: this.gid,
      content: "",
      timestamp: new Date().toISOString()
    };
    this.webSocketSvc.sendMessage(gameAction);
    this.endTurn = true;
    console.log("pressed completeTurn")
  }

  surrender(){
    if(confirm("Are you sure to surrender?")){
      const gameAction: GameAction = {
        action: 'surrender',
        player: this.player,
        gid: this.gid,
        content: "",
        timestamp: new Date().toISOString()
      };
      this.webSocketSvc.sendMessage(gameAction);
    }
  }
// for mat dialog
  constructor(public dialog: MatDialog, public trade: MatDialog) {};
  openLeaderBoard() {
    this.dialog.open(ChartComponent, {
      data: this.gid,
      width: '750px',
      height: '350px',
      // it apply globally, not locally https://stackoverflow.com/questions/48688614/angular-custom-style-to-mat-dialog
      // panelClass: 'custom-dialog-container',
      // backdropClass: 'custom-dialog-backdrop'
    });
  }

  openTrade(){
    this.dialog.open(TradeComponent, {
      data: {
        gid : this.gid,
        playState : this.playerState,
        properties : this.properties,
        // propertyStates : this.propertyStates,
      },
      width: '750px',
      height: '350px',
    });
  }
}
