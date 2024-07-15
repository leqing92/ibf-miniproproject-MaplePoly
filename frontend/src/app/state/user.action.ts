import { createAction, props } from "@ngrx/store";

export const setUser = createAction('[User] Set User', props<{username : string; email : string; gid : string; inGame : boolean}>());
export const updateUser = createAction('[User] Update User', props<{username : string; email : string; gid : string; inGame : boolean}>());
export const clearUser = createAction('[User] Clear User');
