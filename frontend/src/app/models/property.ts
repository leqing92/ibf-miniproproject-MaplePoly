export interface Property{
    id : number;
    name : string;
    cols : string;
    rows : string;
    color : string;
    cost: number;
    classNames : string;
    isOwned : boolean;
    owner : string;
    build : number;
    rent : number[];
}
