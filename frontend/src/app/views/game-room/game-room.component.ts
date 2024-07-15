import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GameCenterService } from '../../service/game-center.service';
import { GameRoom } from '../../models/gameroom';
import { WebSocketService } from '../../service/websocket.service';
import { Message } from '../../models/message';
import { CharacterSummary } from '../../models/character-summary';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { GiphyBottomSheetComponent } from '../../components/giphy-bottom-sheet/giphy-bottom-sheet.component';
import { CustomValidator } from '../../validator/custom-validator';

@Component({
  selector: 'app-game-room',
  templateUrl: './game-room.component.html',
  styleUrl: './game-room.component.css'
})
export class GameRoomComponent implements OnInit, OnDestroy{
  
  private readonly router = inject(Router);
  private readonly gameCenterSvc = inject(GameCenterService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly formBuilder = inject(FormBuilder);
  private readonly webSocketSvc = inject(WebSocketService);
  constructor(private _bottomSheet: MatBottomSheet) {}

  @ViewChild('messageList') messageList!: ElementRef;
  
  gameRoom !: GameRoom;
  gameSetupForm !: FormGroup;
  msgForm!: FormGroup;
  username !: string;
  roomId !: string;
  isOwner : boolean = false;
  hasExitRoom !: boolean;
  availableCharacters!: CharacterSummary[];
  selectedCharacters: Set<string> = new Set();
  messages!: Message[];
  messageContent!: string;

  ngOnInit(): void {
    this.roomId = this.activatedRoute.snapshot.params['id'];
    this.username = localStorage.getItem('username') || '';
    this.messages = [];
    this.hasExitRoom = false;

    this.gameSetupForm = this.formBuilder.group({
      gid: this.formBuilder.control(this.roomId, [Validators.required]),
      players: this.formBuilder.array([], [CustomValidator.minPlayer(2)]),
      salary: this.formBuilder.control(500, [Validators.required, Validators.min(100), Validators.max(1000)]),
      fund: this.formBuilder.control(2000, [Validators.required, Validators.min(500), Validators.max(5000)]),
      diceNo: this.formBuilder.control(2, [Validators.required, Validators.min(1), Validators.max(3)]),
    });

    this.msgForm = this.formBuilder.group({
      msg: this.formBuilder.control("",[Validators.required, Validators.maxLength(250)])
    })

    this.loadGameRoomDetails()
      .then(() => {
        this.loadAvailableCharacters();
        this.initialiseWebSocket();
        // must subscribe else cannot detect the change
        if (this.isOwner) {
          this.gameSetupForm.valueChanges.subscribe(() => {
            this.onGameDetailChange();
          });
        }
      })
      .catch(err => {
        alert(JSON.stringify(err.error));
        this.router.navigate(['/gamecenter']);
        this.hasExitRoom = true;
      });
  }
  
  ngOnDestroy() {
    if (!this.hasExitRoom) {      
      if (!this.isOwner) {
        // cannot apply quitgame() deletegame directly dont know why
        this.gameCenterSvc.quitGameRoom(this.roomId, this.username)
        .then(resp => {
          this.hasExitRoom = true;
          this.leaveGameMsg();
          this.removePlayerFromForm(this.username);
          console.log("quited");
        })
        .catch(err => {
          alert(JSON.stringify(err.error));
        })
        .then(resp=>
          this.webSocketSvc.disconnect()
        );
      } 
      else {
        this.gameCenterSvc.deleteGameRoom(this.roomId)
        .then(resp => {
          this.hasExitRoom = true;
          this.deleteGameMsg();
        })
        .catch(err => {
          alert(JSON.stringify(err.error));
          console.log("exit", err);
        })
        .then(resp=> this.webSocketSvc.disconnect());
      }
    }
    else {
      this.webSocketSvc.disconnect();
    }
  } 
  
  
// load game room detail  
  async loadGameRoomDetails(): Promise<void> {
    const resp = await this.gameCenterSvc.getGameRoomById(this.roomId);
    this.gameRoom = resp;
    this.isOwner = this.gameRoom.owner === this.username;
    this.initializeForm();
  }

  loadAvailableCharacters(): void {
    this.gameCenterSvc.getAvailableCharacters()
    .then(characters => {
      this.availableCharacters = characters;
    })
    .catch(err => {
      console.error('Failed to load available characters:', err);
    });
  }

  private initialiseWebSocket(): void{
    // this.webSocketSvc.connect(`ws://localhost:8080/app/room/${this.roomId}`);
    this.webSocketSvc.connect(`/app/room/${this.roomId}`);
      this.webSocketSvc.messages$.subscribe(message => {
        this.handleMessage(message);
      });
      // on connection establish, then only send join message to everyone in the room; else error
      this.webSocketSvc.connection$.subscribe(() => {
        this.joinGameMsg();
        this.requestCurrentState();
      });
  }

// form detail
  get players(): FormArray {
    return this.gameSetupForm.get('players') as FormArray;
  }

  playerName(index : number) {
    return this.gameRoom.players[index];
  }

  initializeForm() {
    this.gameRoom.players.forEach(player => {
      this.addPlayerToForm(player);
    });

    //if form group there add but no reflect
    // this.gameSetupForm.patchValue({
    //   gid: this.gameRoom.gid
    // });
  }

  addPlayerToForm(playerName: string): void {
    this.players.push(this.formBuilder.group({
      name: [playerName, Validators.required],
      character: ['', Validators.required]
    }));
  }

  removePlayerFromForm(playerName: string): void {
    const playerIndex = this.gameRoom.players.indexOf(playerName);
    if (playerIndex !== -1) {      
      this.gameRoom.players.splice(playerIndex, 1);
    }
    // Remove player from FormArray
    const playerControls = this.players.controls;
    for (let i = 0; i < playerControls.length; i++) {
      const control = playerControls[i] as FormGroup;
      if (control.get('name')?.value === playerName) {
        this.players.removeAt(i);
        break;
      }
    }       
  }

  // isPlayerAllowedToModify(playerName: string): boolean {
  //   return playerName === this.username;
  // }
  
  selectCharacter(character: CharacterSummary): void {
    const currentPlayerIndex = this.gameRoom.players.indexOf(this.username);
    if (currentPlayerIndex !== -1) {
      const playerFormGroup = this.players.at(currentPlayerIndex) as FormGroup;

      playerFormGroup.get('character')?.setValue(character.name);
      this.onCharacterChange(currentPlayerIndex);
    }
  }

  onCharacterChange(index: number): void {
    const selectedCharacter = this.players.at(index).get('character')?.value;
    const message = {
      type: 'select',
      sender: this.username,
      timestamp: new Date().toISOString(),
      content: selectedCharacter
    };
    this.webSocketSvc.sendMessage(message);
  }

  onGameDetailChange(){
    const gamedetailOk : boolean = !(this.gameSetupForm.hasError("salary") && 
      this.gameSetupForm.hasError("fund") && 
      this.gameSetupForm.hasError("diceNo") );

    if(this.isOwner && gamedetailOk){
      const gameDetails = {
        salary : this.gameSetupForm.get("salary")?.value,
        fund : this.gameSetupForm.get("fund")?.value,
        diceNo : this.gameSetupForm.get("diceNo")?.value
      };
      // console.log(gameDetails)
      const message = {
        type: 'updateGameDetail',
        sender: this.username,
        timestamp: new Date().toISOString(),
        content: JSON.stringify(gameDetails)
      };      
      this.webSocketSvc.sendMessage(message);
    }
  }

  requestCurrentState(): void {
    const message: Message = {
      type: 'currentStateRequest',
      sender: this.username,
      timestamp: new Date().toISOString(),
      content: ''
    };
    this.webSocketSvc.sendMessage(message);
  }

// game room function
  sendmsg(){    
    if(this.msgForm.valid){
      const message : Message = {
        type: 'message',
        sender: this.username,
        timestamp : new Date().toISOString(),
        content: this.msgForm.value['msg']
      };
      this.webSocketSvc.sendMessage(message);
      this.msgForm.reset();
    }  
  }

  startGame() {   
    if (this.gameSetupForm.valid) {
      const formData = this.gameSetupForm.value;
      this.gameCenterSvc.sendGameSetupData(this.roomId, formData)
      .then(resp => {
          // localStorage.setItem("formData", JSON.stringify(formData));
          this.startGameMsg();
          this.gameCenterSvc.properties = resp;
        })
      .catch(err =>{
        alert(JSON.stringify(err.error));
      })
      .then(resp => {
        this.hasExitRoom = true;
        this.router.navigate([`/game/${this.roomId}`]);
      });
    }
  } 

  async quitGame() : Promise<void>{
    this.gameCenterSvc.quitGameRoom(this.roomId, this.username)
      .then(resp => {
        // console.log(resp);
        this.hasExitRoom = true;
        this.leaveGameMsg();
        this.removePlayerFromForm(this.username);
        this.router.navigate(['/gamecenter']);
        // console.log("quited");
      })
      .catch(err => {
        alert(JSON.stringify(err.error));
        this.router.navigate(['/gamecenter']);
      });
  }

  async deleteGame() : Promise<void> {    
    this.gameCenterSvc.deleteGameRoom(this.roomId)
      .then(resp => {
        // console.log(resp);
        this.hasExitRoom = true;
        this.deleteGameMsg();
        this.router.navigate(['/gamecenter']);
      })
      .catch(err => {
        alert(JSON.stringify(err.error));
        console.log("delete", err);
        this.router.navigate(['/gamecenter']);
      });
  }
  
  //websocket related function ---------------------------------------------------------------------------------
  handleMessage(message: Message): void {
    switch (message.type) {
      case 'message':
        this.messages.push({
          type: 'system',
          sender: message.sender,
          timestamp: message.timestamp,
          content: `<strong>${message.sender}</strong>: ${message.content}`
        });
        setTimeout(() => {
          this.scrollToBottom();
        }, 0);
        break;

      case 'join':
        // check is new player or not
        if (!this.gameRoom.players.includes(message.sender)) {
          this.messages.push({
            type: 'system',
            sender: message.sender,
            timestamp: message.timestamp,
            content: `<strong>${message.sender}</strong> joined the game`
          });
          this.addPlayerToForm(message.sender);
          this.gameRoom.players.push(message.sender);
          setTimeout(() => {
            this.scrollToBottom();
          }, 0);
        } 
        else {
          // player already exist, update the character selection
          const playerIndex = this.gameRoom.players.indexOf(message.sender);
          if (playerIndex !== -1) {
            const existingPlayer = this.players.at(playerIndex) as FormGroup;
            if (!existingPlayer.get('character')?.value) {
              // update character selection if it's not already set
              this.updateCharacterSelection(message.sender, message.content);
            }
          }
        }
        this.requestCurrentState(); // for the in game player get the re-join player selection
        break;

      case 'currentState':
        this.updateCurrentState(message.content);
        break;      
    
      case 'select':
        const playerName = message.sender;
        const selectedCharacter = message.content;
        this.updateCharacterSelection(playerName, selectedCharacter);
        break;

      case 'updateGameDetail':          
          this.updateGameDetail(message);
          break;

      case 'leave':
        this.messages.push({
          type: 'system',
          sender: message.sender,
          timestamp: message.timestamp,
          content: `<strong>${message.sender}</strong> left the game`
        });
        // to scroll to the bottomost always
        setTimeout(() => {
          this.scrollToBottom();
        }, 0);
        this.removePlayerFromForm(message.sender); 
        break
        
      case 'delete':
        alert(`${message.sender} deleted the game`);
        this.router.navigate(['/gamecenter']);
        this.hasExitRoom = true;
        break
      
      case 'start':
        // localStorage.setItem("formData", message.content);
        this.router.navigate([`/game/${this.roomId}`]);       
        this.hasExitRoom = true;
        break

      default:
        console.warn('Unsupported message type:', message);
    }
  } 

  joinGameMsg(): void {
    const message : Message = {
      type: 'join',
      sender: this.username,      
      timestamp : new Date().toISOString(),
      content: ''
    };
    this.webSocketSvc.sendMessage(message);   
  }

  updateCurrentState(currentState: any): void {
    const state = JSON.parse(currentState);
    this.gameRoom.players.forEach((player, index) => {
      if (state[player]) {
        this.updateCharacterSelection(player, state[player]);
      }
    });
  }
  
  updateCharacterSelection(playerName: string, selectedCharacter: string): void {
    const playerIndex = this.gameRoom.players.indexOf(playerName);
    // console.log("index", playerIndex);
    if (playerIndex !== -1) {
      const formGroup = this.players.at(playerIndex) as FormGroup;
      formGroup.patchValue({ character: selectedCharacter });
    }
  }

  updateGameDetail(message : Message){
    const gameDetails = JSON.parse(message.content);
    // { emitEvent: false } prevent re-trigger for owner on the form change
    this.gameSetupForm.patchValue(gameDetails, { emitEvent: false });
  }

  leaveGameMsg(): void {
    const message : Message = {
      type: 'leave',
      sender: this.username,      
      timestamp : new Date().toISOString(),
      content: ''
    };
    this.webSocketSvc.sendMessage(message);
    // console.log("sent");
  }

  deleteGameMsg(): void {
    const message : Message = {
      type: 'delete',
      sender: this.username,      
      timestamp : new Date().toISOString(),
      content: ''
    };
    this.webSocketSvc.sendMessage(message);
  }

  startGameMsg(): void {
    const message : Message = {
      type: 'start',
      sender: this.username,      
      timestamp : new Date().toISOString(),
      content: ""
    };
    this.webSocketSvc.sendMessage(message);
  }

  scrollToBottom(): void {
    try {
      this.messageList.nativeElement.scrollTop = this.messageList.nativeElement.scrollHeight;
    } catch(err) { }
  }

  openBottomSheet(): void {
    const bottomSheetRef = this._bottomSheet.open(GiphyBottomSheetComponent);
    bottomSheetRef.instance.giphySelected.subscribe((giphyUrl: string) => {
      const message: Message = {
        type: 'message',
        sender: this.username,
        timestamp: new Date().toISOString(),
        content: `<img src="${giphyUrl}" alt="GIPHY">`
      };
      this.webSocketSvc.sendMessage(message);
    });
  }  
}
