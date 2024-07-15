import { SpriteAnimations } from "./spriteanimation";

export class Character{
  url !: string;
  dieUrl !: string;
  spriteurl !: string;
  name !: string;
  // individual frame fir eg:
  // shawdow dog resolution : 6876 x 5230 (w x h) @ 12columns * 10rows
  // ↓ 6876 aprox. = 573 
  spriteWidth !: number;
  spriteHeight !: number;    
  spritesheet !: SpriteAnimations;
  
  constructor(url : string, dieUrl : string, spriteurl : string, name : string, spriteWidth : number, spriteHeight : number, spritesheet : SpriteAnimations){
      this.url = url;
      this.dieUrl = dieUrl;
      this.spriteurl = spriteurl;
      this.name = name;
      this.spriteWidth = spriteWidth;
      this.spriteHeight = spriteHeight;        
      this.spritesheet = spritesheet;
  }
                                 
}