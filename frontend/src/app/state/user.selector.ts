import { UserState } from "../models/user";
import { createFeatureSelector, createSelector } from "@ngrx/store";

export const selectUserState = createFeatureSelector<UserState>('user');

export const selectUser = createSelector(selectUserState, (state: UserState) => state);
// export const selectUsername = createSelector(selectUserState, (state: UserState) => state.username);
// export const selectEmail = createSelector(selectUserState, (state: UserState) => state.email);
// export const selectGid = createSelector(selectUserState, (state: UserState) => state.gid);
// export const selectInGame = createSelector(selectUserState, (state: UserState) => state.inGame);