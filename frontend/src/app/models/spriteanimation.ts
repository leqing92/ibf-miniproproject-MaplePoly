export interface SpriteFrameCoordinates{   
    x: number;
    y: number;    
}

export interface SpriteAnimation {
    coordinates: SpriteFrameCoordinates[];
}

export interface SpriteAnimations{
    [key :string] : SpriteAnimation;
}