import { createAction, props } from "@ngrx/store";
import { PlayerState, PropertyState } from "../models/gamestate";

//Property Action
export const setPropertyStates = createAction('[Property] Set Property States', props<{ propertyStates: PropertyState[] }>());
export const updatePropertyState = createAction('[Property] Update Property State', props<{ propertyState: PropertyState }>());
// export const loadPropertyStates = createAction('[Property] Load Property States'); //for effect but i dont hv http call, i use websocket 

//Player Action
export const setPlayerStates = createAction('[Player] Set Player States', props<{ playerStates: PlayerState[] }>());
export const updatePlayerState = createAction('[Player] Update Player State', props<{ playerState: PlayerState }>());