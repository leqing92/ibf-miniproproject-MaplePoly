export interface ChartData{
    labels : number[];
    datasets : DataSet[];
}

export interface DataSet{
    label : string;
    money : number[];
    propertyValue : number[];
    total : number[];
}