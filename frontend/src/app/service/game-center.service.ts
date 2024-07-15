import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, firstValueFrom } from "rxjs";
import { Property } from "../models/property";
import { GameRoom } from "../models/gameroom";
import { CharacterSummary } from "../models/character-summary";
import { GameRoomData } from "../models/gameroomdata";

Injectable()

export class GameCenterService{

    private readonly http = inject(HttpClient);

    properties !:Property[];

    createGameRoom(name : string) : any{
        const params = new HttpParams()
            .set("name", name);

        return firstValueFrom(this.http.post('/api/gamerooms/create', params));
    }

    getGameRoomList(): Observable<GameRoom[]>{
        return this.http.get<GameRoom[]>('/api/gamerooms/list');
    }

    getGameRoomById(gid :string) : Promise<GameRoom>{
        return firstValueFrom(this.http.get<GameRoom>(`/api/gamerooms/get/${gid}`));
    }

    getAvailableCharacters() : Promise<CharacterSummary[]>{
        return firstValueFrom(this.http.get<CharacterSummary[]>('/api/gamerooms/chac'));
    }

    joinGameRoom(gid :string, name : string ){
        const params = new HttpParams()
            .set("name", name);

        return firstValueFrom(this.http.post<GameRoom[]>(`/api/gamerooms/join/${gid}`, params));
    }

    quitGameRoom(gid :string, name : string ){
        const params = new HttpParams()
            .set("name", name);
        return firstValueFrom(this.http.post<GameRoom[]>(`/api/gamerooms/quit/${gid}`, params, {responseType: 'text' as 'json'}));
    }

    deleteGameRoom(gid :string){
        return firstValueFrom(this.http.delete<GameRoom[]>(`/api/gamerooms/delete/${gid}`, {responseType: 'text' as 'json'}));
    }
    
    // giphy search
    giphysearch(q : string) : Promise<string[]>{
        const params = new HttpParams()
                        .set('q', q)
        return firstValueFrom(this.http.post<string[]>('/api/gamerooms/giphy/search', params));
    }

//start game
    sendGameSetupData(id : string, formData : GameRoomData): Promise<Property[]> {
        const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
        return firstValueFrom(this.http.post<Property[]>(`/api/gamerooms/room/${id}`, formData, { headers }));
    }
}