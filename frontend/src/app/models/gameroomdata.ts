export interface GameRoomData{
    gid : string;
    players : Player[]
}

export interface Player{
    name: string;
    character: string;
}