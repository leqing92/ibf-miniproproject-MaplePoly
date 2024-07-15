import { createReducer, on } from "@ngrx/store";
import { initialPlayerStates, initialPropertyStates } from "../models/gamestate";
import { setPlayerStates, setPropertyStates, updatePlayerState, updatePropertyState } from "./game.action";

//Property State Reducer
export const propertyReducer = createReducer(
  initialPropertyStates,
  on(setPropertyStates, (state, { propertyStates }) => ({ ...state, propertyStates })),
  on(updatePropertyState, (state, { propertyState }) => ({
      ...state,
      propertyStates: state.propertyStates.map(ps => ps.name === propertyState.name ? propertyState : ps)
  }))
);

// Player State Reducer
export const playerReducer = createReducer(
  initialPlayerStates,
  on(setPlayerStates, (state, { playerStates }) => ({ ...state, playerStates })),
  on(updatePlayerState, (state, { playerState }) => ({
    ...state,
    playerStates: state.playerStates.map(ps => ps.name === playerState.name ? playerState : ps)
  }))
  // {
  //   const index = state.playerStates.findIndex(p => p.name === playerState.name);
  //   if (index !== -1) {
  //     const updatedPlayerStates = [...state.playerStates];
  //     updatedPlayerStates[index] = playerState;
  //     return { ...state, playerStates: updatedPlayerStates };
  //   }
  //   return { ...state, playerStates: [...state.playerStates, playerState] };
  // })
);
