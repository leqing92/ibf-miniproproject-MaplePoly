export interface User{
    id: string;
    username : string;
    password : string;
    email: string;
    dob: Date;
}

export interface UserState{
    username: string;
    email: string;
    gid: string;
    inGame: boolean;
}

export const initialUserState: UserState = {
    username: '',
    email: '',
    gid:'',
    inGame: false
};