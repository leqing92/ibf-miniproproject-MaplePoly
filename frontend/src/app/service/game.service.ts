import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { firstValueFrom } from "rxjs";
import { Property } from "../models/property";
import { Character } from "../models/character";
import { GameRoomData } from "../models/gameroomdata";
import { ChartData } from "../models/chart";

Injectable()

export class GameService{

    private readonly http = inject(HttpClient);

    // getPropertyList(): Promise<Property[]> {
    //     return firstValueFrom(this.http.get<Property[]>('/api/game/property'));
    // }

    // getCharacterList(formdata: GameRoomData): Promise<Character[]> {
    //     return firstValueFrom(this.http.post<Character[]>('/api/game/chac', formdata));
    // }

    getGameBasicInfo(id : string): Promise<{characters : Character[], properties : Property[]}> {
        return firstValueFrom(this.http.get<{characters : Character[], properties : Property[]}>(`/api/game/${id}/data`));
    }

    getGameChartInfo(id : string): Promise<ChartData>{
        return firstValueFrom(this.http.get<ChartData>(`/api/game/${id}/chart`));
    }
}