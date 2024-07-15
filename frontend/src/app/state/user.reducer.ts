import { Action, createReducer, on } from "@ngrx/store";
import { UserState, initialUserState } from "../models/user";
import * as UserAction from "./user.action";

export const userReducer = createReducer(
    initialUserState,
    on(UserAction.setUser, (state, {username, email, gid, inGame}) => ({...state, username, email, gid, inGame})),
    on(UserAction.updateUser, (state, {username, email, gid, inGame}) => ({...state, username, email, gid, inGame})),
    on(UserAction.clearUser, state => ({...state, username : "", email : "", gid : "", inGame : false}))
)