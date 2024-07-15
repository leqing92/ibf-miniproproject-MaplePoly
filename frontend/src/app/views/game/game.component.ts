import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from '../../service/game.service';
import { GameRoomData } from '../../models/gameroomdata';
import { Character } from '../../models/character';
import { Property } from '../../models/property';
import { WebSocketService } from '../../service/websocket.service';
import { GameAction, GameState, PlayerState, PropertyState, TurnState } from '../../models/gamestate';
import { Message } from '../../models/message';
import { Store } from '@ngrx/store';
import { setPlayerStates, setPropertyStates } from '../../state/game.action';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { GiphyBottomSheetComponent } from '../../components/giphy-bottom-sheet/giphy-bottom-sheet.component';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrl: './game.component.css'
})
export class GameComponent implements OnInit, OnDestroy{

  private readonly gameSvc = inject(GameService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly webSocketSvc = inject(WebSocketService);
  private readonly store = inject(Store);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
 
  constructor(private _bottomSheet: MatBottomSheet) {}

  gid !: string;
  username !: string;
  formData !: GameRoomData;

  steps : { [key: string]: number } = {};
  currentTileIds : { [key: string]: number } = {};
  characterPositions : { [key: string]: { top: string, left: string, isMirrored: boolean } } = {};
  isMoving : {[key : string] : boolean } = {};
  chacState :{[key : string] : string } = {}; // idle | run | ko
  movingIntervals : { [key: string]: any } = {};  

  properties !: Property[];
  characters: Character[] = [];
  offset !: number; // use to allow the character card display properly in html
  playerStates : PlayerState[] = []; 
  propertyStates : PropertyState[] = [];
  battlelogs!: Message[];
  messages !: Message[];
  msgForm!: FormGroup;
  @ViewChild('messageList') messageList!: ElementRef;
  @ViewChild('battlelogList') battlelogList!: ElementRef;

  ngOnInit(): void {    
    this.gid = this.activatedRoute.snapshot.params['id'];
    this.username = localStorage.getItem('username') || '';
    this.battlelogs = [];
    this.messages = [];

    this.msgForm = this.fb.group({      
      msg: this.fb.control("",[Validators.required, Validators.maxLength(250)])
    })
    
    // this.webSocketSvc.connect(`ws://localhost:8080/app/game/${this.gid}`);
    this.webSocketSvc.connect(`/app/game/${this.gid}`);
    this.webSocketSvc.messages$.subscribe(message => {
      this.handleMessage(message);
    });

    // on connection establish, send join message to everyone in the room
    this.webSocketSvc.connection$.subscribe(() => {      
      this.requestGameState(this.gid);
    });
    // hrrp call for basic info
    this.getInitialGameDetail();
  }

  ngOnDestroy() {
    this.webSocketSvc.disconnect();
    // Clear the interval to prevent memory leaks
    Object.keys(this.movingIntervals)
      .forEach(playerName => 
        clearInterval(this.movingIntervals[playerName])
      );
  }

  async getInitialGameDetail(): Promise<void> {
    try {      
      this.gameSvc.getGameBasicInfo(this.gid)
        .then((result) => {
          this.properties = result.properties as Property[];
          this.characters = result.characters as Character[];
          
          if(this.characters.length <= 3){
            this.offset = 1;
          }
          else{
            this.offset = 2;
          }
        })
        .catch((err) => {
          alert(err.error);
          this.router.navigate(['/gamecenter'])
        });
    }
    catch (err) {
      alert(err);
    }   
  }

  sendmsg(){    
    if(this.msgForm.valid){
      const message : GameAction = {
        action :  'chat',
        player : this.username,
        gid : this.gid,
        content: this.msgForm.value['msg'],
        timestamp : new Date().toISOString()       
      };
      this.webSocketSvc.sendMessage(message);
      this.msgForm.reset();
    }  
  }

  openBottomSheet(): void {
    const bottomSheetRef = this._bottomSheet.open(GiphyBottomSheetComponent);
    bottomSheetRef.instance.giphySelected.subscribe((giphyUrl: string) => {
      const message: GameAction = {
        action: 'chat',
        player: this.username,
        gid : this.gid,
        timestamp: new Date().toISOString(),
        content: `<img src="${giphyUrl}" alt="GIPHY">`
      };
      this.webSocketSvc.sendMessage(message);
    });
  }

  handleMessage(gameAction: GameAction): void{
    // console.log(gameAction);
    switch (gameAction.action) {
      case 'currentStateRequest':
        // console.log(gameAction);
        this.handleGameState(gameAction.content);
        break;

      case 'rollDice':
        // console.log(gameAction);
        this.moveCharacter(gameAction);        
        break;
      
      case 'passGo':
        this.handlePassGo(gameAction);
        break;
      
      case 'jailTurn':
        this.battlelogs.push({
          type: 'system',
          sender: 'System',
          timestamp: gameAction.timestamp,
          content: gameAction.content
        });
        setTimeout(() => {
          this.scrollToBottom('battlelog');
        }, 0);
        break;

      case 'toJail':
        this.handleToJail(gameAction);
        break;

      case "payRent":
        this.handlePayRent(gameAction);
        break;

      case "buyProperty":
        this.handleBuyProperty(gameAction);
        break;
      
      case "build":
        this.handleBuild(gameAction);
        break;
      
      case "sell":
        this.handleSell(gameAction);
        break;
      
      case "endTurn":
        this.handleEndTurn(gameAction);
        break;
      // use for the last one move/all in jail and invoke endturn check at backend
      case "plsEndTurn":
        this.invokeEnturn();
        break;
      
      case "newTurn":
        this.handleNewTurn(gameAction);
        break;
      
      case "bankrupt":
        this.handleBankrupt(gameAction);
        break;
        
      case "lose":
        alert(gameAction.content);
        // if(confirm(gameAction.content + "\nClick ok to return to Game Center. \nPlease note that once you will not able to join back the game once you leave.")){
        this.router.navigate(['/game', this.gid, 'leaderboard']);
        // }
        break;

      case "win":
        this.handleWin(gameAction);
        break;

      case "message":
        this.battlelogs.push({
          type: 'system',
          sender: 'System',
          timestamp: gameAction.timestamp,
          content: gameAction.content
        });
        setTimeout(() => {
          this.scrollToBottom('battlelog');
        }, 0);
        break;

      case "chat":
        this.messages.push({
          type: 'system',
          sender: gameAction.player,
          timestamp: gameAction.timestamp,
          content: gameAction.content
        })
        setTimeout(() => {
          this.scrollToBottom('message');
        }, 0);
        break;

      default:
        alert('Unsupported message type: ' + JSON.stringify(gameAction));
    }
  }

  scrollToBottom(type: 'message' | 'battlelog'): void {
    if (type === 'message') {
      try {
        this.messageList.nativeElement.scrollTop = this.messageList.nativeElement.scrollHeight;
      } catch (err) {
        // console.error(err);
      }
    } else if (type === 'battlelog') {
      try {
        this.battlelogList.nativeElement.scrollTop = this.battlelogList.nativeElement.scrollHeight;
      } catch (err) {
        // console.error(err);
      }
    }
  }

  requestGameState(gid: string): void {
    const gameaction : GameAction = { 
      action :  'currentStateRequest',
      player : this.username,
      gid : gid,
      content : '',
      timestamp : new Date().toISOString()
    };
    this.webSocketSvc.sendMessage(gameaction);
  }

  handleGameState(content : string){
    const turnState = JSON.parse(content) as TurnState;

    this.propertyStates = turnState.propertyStates;
    this.playerStates = turnState.playerStates;

    // save to NgRx Store
    this.store.dispatch(setPropertyStates({propertyStates : this.propertyStates}));
    this.store.dispatch(setPlayerStates({playerStates : this.playerStates}));

    // update character position
    this.playerStates.forEach(playerState => {
      this.updateCharacterPosition(playerState.name, playerState.position || 0);
      this.currentTileIds[playerState.name] = playerState.position || 0;
      this.chacState[playerState.name] = "idle";
      if(playerState.hasBankrupted || playerState.inJail){
        this.chacState[playerState.name] = 'ko';
      }
    });

  }

  updateGameStateByTurnState(turnStateString : string){
    const turnState = JSON.parse(turnStateString) as TurnState;
    
    this.propertyStates = turnState.propertyStates;
    this.playerStates = turnState.playerStates;

    // save to NgRx Store
    this.store.dispatch(setPropertyStates({propertyStates : this.propertyStates}));
    this.store.dispatch(setPlayerStates({playerStates : this.playerStates}));
  }
  
  updatePlayerState(playerStates: PlayerState[]) {
    this.playerStates = playerStates;
    this.store.dispatch(setPlayerStates({playerStates : this.playerStates}));    
  }

  updatePropertyState(propertyStates: PropertyState[]) {
    this.propertyStates = propertyStates;
    this.store.dispatch(setPropertyStates({propertyStates : this.propertyStates}));
  }

  moveCharacter(gameAction: GameAction) {
    const newPlayerState : PlayerState = JSON.parse(gameAction.content) as PlayerState;
    const playerName = newPlayerState.name;
    const currentPosition = this.playerStates.find(player => player.name === playerName)?.position || 0;

    this.steps[playerName] = newPlayerState.position - currentPosition;
    if (this.steps[playerName] < 0) {
      this.steps[playerName] += 40;
    }

    this.playerStates = this.playerStates.map(player =>
        player.name === playerName ? newPlayerState : player
      );  

    this.battlelogs.push({
      type: 'system',
      sender: "System",
      timestamp: gameAction.timestamp,
      content: `<strong>${playerName}</strong> rolled the dice and moved ${this.steps[playerName]} steps`
    });    

    if (!this.isMoving[playerName]) {
      this.chacState[playerName] = "run";
      this.isMoving[playerName] = true;
      this.movingIntervals[playerName] = setInterval(() => {
        if (this.steps[playerName] > 0) {
          this.currentTileIds[playerName] = (this.currentTileIds[playerName] + 1) % 40; // Move one tile forward
          this.updateCharacterPosition(playerName, this.currentTileIds[playerName]);
          this.steps[playerName]--;
        } else {
          clearInterval(this.movingIntervals[playerName]);                  
          this.isMoving[playerName] = false;
          this.chacState[playerName] = "idle";
          // for change the player location if at go to jail
          if(this.currentTileIds[playerName] == 30){
            this.updateCharacterPosition(playerName, 10);
            this.currentTileIds[playerName] = 10;
            this.chacState[playerName] = "ko";
          }
        }
      }, 500); 
    }
    
    this.store.dispatch(setPlayerStates({playerStates : this.playerStates}));
  }

  handlePassGo(gameAction: GameAction){
    const newPlayerStates : PlayerState[] = JSON.parse(gameAction.content) as PlayerState[];
    this.updatePlayerState(newPlayerStates);

    this.battlelogs.push({
      type: 'system',
      sender: "System",
      timestamp: gameAction.timestamp,
      content: `<strong>${gameAction.player} </strong> passed 'GO' and earn salary.`
    })    
  }

  handleToJail(gameAction: GameAction){
    const playStates = JSON.parse(gameAction.content) as PlayerState[];
    this.updatePlayerState(playStates);
  }

  handlePayRent(gameAction: GameAction){
    const playStates = JSON.parse(gameAction.content) as PlayerState[];
    this.updatePlayerState(playStates);
  }

  handleBuyProperty(gameAction: GameAction){
    this.updateGameStateByTurnState(gameAction.content);
  }

  handleBuild(gameAction: GameAction){
    this.updateGameStateByTurnState(gameAction.content);
  }

  handleSell(gameAction : GameAction){
    this.updateGameStateByTurnState(gameAction.content);
  }

  handleEndTurn(gameAction: GameAction){
    const turnState = JSON.parse(gameAction.content) as TurnState;
    this.updateGameStateByTurnState(gameAction.content);
  }

  invokeEnturn(){
    const gameAction: GameAction = {
      action: 'endTurn',
      player: this.username,
      gid: this.gid,
      content: "",
      timestamp: new Date().toISOString()
    };
    this.webSocketSvc.sendMessage(gameAction);
  }

  handleNewTurn(gameAction: GameAction){
    const turnState = JSON.parse(gameAction.content) as TurnState;
    this.updateGameStateByTurnState(gameAction.content);
  }

  handleBankrupt(gameAction: GameAction){
    const turnState = JSON.parse(gameAction.content) as TurnState;
    this.updateGameStateByTurnState(gameAction.content);
    this.chacState[gameAction.player] = 'ko';
  }

  handleWin(gameAction : GameAction){
    this.battlelogs.push({
      type: 'system',
      sender: 'System',
      timestamp: gameAction.timestamp,
      content: `${gameAction.content}`
    })
    alert(gameAction.content)
    // if(confirm(gameAction.content + "\nClick ok to return to Game Center. \nPlease note that once you will not able to join back the game once you leave.")){
    this.router.navigate(['/game', this.gid, 'leaderboard']);
    // }
  }

  updateCharacterPosition(playerName: string, tileId: number) {
    let col = 0;
    let row = 0;

    if (tileId >= 0 && tileId <= 10) {
      // Move right
      col = tileId;
      row = 0;
    } else if (tileId >= 11 && tileId <= 20) {
      // Move down
      col = 10;
      row = tileId - 10;
    } else if (tileId >= 21 && tileId <= 30) {
      // Move left
      col = 30 - tileId;
      row = 10;
    } else if (tileId >= 31 && tileId <= 39) {
      // Move up
      col = 0;
      row = 40 - tileId;
    }

    this.characterPositions[playerName] = {
      top: `${row * 100}%`,
      left: `${col * 100}%`,
      isMirrored: tileId > 10 && tileId < 31
    };
  }

  //to add check which character is this so to confirm translate X and translate Y
  getCharacterStyle(playerName: string) {
    const position = this.characterPositions[playerName] || { top: '0%', left: '0%', isMirrored: false }; 
    const scaleX = position.isMirrored ? -1 : 1;    
    const translationMap = [
      { x: 0, y: -5 },
      { x: 0, y: 30 },
      { x: 32, y: -5 },
      { x: 32, y: 30 }
    ];

    // get the index of the player
    const playerIndex = this.playerStates.findIndex(player => player.name === playerName);    
    //ensure index is within the range of the translationMap
    const translation = translationMap[playerIndex % translationMap.length];

    const translateX = position.isMirrored ? -translation.x : translation.x;
    const translateY = translation.y;

    return {
      top: position.top,
      left: position.left,
      transform: `scaleX(${scaleX}) translateX(${translateX}px) translateY(${translateY}px)`,
      transformOrigin: `20px` // to adjust this also if change character's cancas CSS size (now 40px)
    };
  }

  // for draggable button
  initialX: number = 0;
  initialY: number = 0;
  offsetX: number = 370;
  offsetY: number = 420;

  onDragStart(event: DragEvent) {
    if (event.dataTransfer) {
      this.initialX = event.clientX - this.offsetX;
      this.initialY = event.clientY - this.offsetY;
      event.dataTransfer.setDragImage(new Image(), 0, 0); // Prevent default drag image
    }
  }

  onDragEnd(event: DragEvent) {
    this.offsetX = event.clientX - this.initialX;
    this.offsetY = event.clientY - this.initialY;
    const gameFunction = event.target as HTMLElement;
    gameFunction.style.transform = `translate(${this.offsetX}px, ${this.offsetY}px)`;
  }
}


