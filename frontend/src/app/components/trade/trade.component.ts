import { Component, Inject, OnInit, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { GameService } from '../../service/game.service';
import { GameAction, PlayerState, PropertyState } from '../../models/gamestate';
import { Property } from '../../models/property';
import { WebSocketService } from '../../service/websocket.service';

@Component({
  selector: 'app-trade',
  templateUrl: './trade.component.html',
  styleUrl: './trade.component.css'
})
export class TradeComponent implements OnInit{

  private readonly webSocketSvc = inject(WebSocketService);
  
  gid !: string;
  playerState !: PlayerState;
  properties !: Property[];
  propertyStates !: PropertyState[];
  // must initialise fisrt else cannot push in init
  propertyList : { 
    id : number,
    property : string,
    price : number    
  }[] = [];
  
  constructor(
    public dialogRef: MatDialogRef<TradeComponent>,
    @Inject(MAT_DIALOG_DATA) public data : {
      gid : string,
      playState : PlayerState,
      properties : Property[],
      // propertyStates : PropertyState[],
    },
  ){}

  ngOnInit(): void {
    this.playerState = this.data.playState;
    this.properties = this.data.properties;
    // this.propertyStates = this.data.propertyStates;
    this.gid = this.data.gid;
    
    this.playerState.properties.forEach(
      id => {
        const property = this.properties.find(p => p.id === id);

        if(property){
          this.propertyList.push({
            id : property.id,
            property : property.name,
            price : property.cost
          })
        }
      })

      this.propertyList.sort((a, b) => a.id - b.id);
  }

  sell(id : number) : void {
    if(confirm("Are you sure to sell the property for $" + this.properties.at(id)?.cost)){
      const gameAction : GameAction = {
        action: 'sell',
        player: this.playerState.name,
        gid: this.gid,
        content: id.toString(),
        timestamp: new Date().toISOString()
      };
  
      this.webSocketSvc.sendMessage(gameAction);
      
      // update the list
      this.propertyList = this.propertyList.filter(prop => prop.id !== id);
    }
  }

}
