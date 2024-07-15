export interface GameState{
    id : string;
    turnStates : TurnState[];
    isEnd : boolean;
}

export interface TurnState{
    currentTurn : number;
    playerStates : PlayerState[];
    propertyStates : PropertyState[];
}

export interface PlayerState{
    name : string;
    character : string;
    properties : number[];
    money : number;
    position : number;
    hasMoved : boolean;
    endTurn : boolean;
    inJail : boolean;
    hasBankrupted : boolean;
}

export interface PropertyState{
    id : number;
    name : string;
    canBuy: boolean;
    isOwned : boolean;
    owner : string;
    star : number;
    rent : number;
}

export interface GameAction{
    action : string;
    player : string;
    gid : string;
    content : string;
    timestamp : string;
}

// export const initialPropertyState: PropertyState = {
//     name : "",
//     isOwned : false,
//     owner : "",
//     star : 0,
//     rent : 0
// };

// Property State NgRx Store
export interface PropertyStateStore {
    propertyStates: PropertyState[];
}

export const initialPropertyStates: PropertyStateStore = {
    propertyStates: []
}

// Player State NgRx Store
export interface PlayerStateStore{
    playerStates: PlayerState[];
}

export const initialPlayerStates: PlayerStateStore = {
    playerStates: []
}