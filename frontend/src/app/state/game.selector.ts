import { createFeatureSelector, createSelector } from "@ngrx/store";
import { PlayerState, PlayerStateStore, PropertyStateStore } from "../models/gamestate";

// Property States Selector 
// to match as module
export const selectPropertyState = createFeatureSelector<PropertyStateStore>('property');

export const selectAllPropertyStates = createSelector(
    selectPropertyState,
    (state: PropertyStateStore) => state.propertyStates
);

export const selectPropertyStateById = (id: number) => createSelector(
    selectPropertyState,
    (state: PropertyStateStore) => state.propertyStates.find(ps => ps.id === id)
);

// Player State Selector
export const selectPlayerStateModel = createFeatureSelector<PlayerStateStore>('player');

export const selectAllPlayerStates = createSelector(
  selectPlayerStateModel,
  (state: PlayerStateStore) => state.playerStates
);

export const selectPlayerStateByName = (name: string) => createSelector(
  selectPlayerStateModel,
  (state: PlayerStateStore) => state.playerStates.find(player => player.name === name)
);