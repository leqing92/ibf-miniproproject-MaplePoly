import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Character } from '../../models/character';
import { SpriteAnimation } from '../../models/spriteanimation';

@Component({
  selector: 'app-character',
  templateUrl: './character.component.html',
  styleUrl: './character.component.css'
})
export class CharacterComponent {
  @Input() character!: Character;
    spriteSheetUrl!: string;
    spriteWidth!: number;
    spriteHeight!: number;
    spriteAnimations: { [key: string]: SpriteAnimation } = {};

  @Input() staggerFrames: number = 30;
  @Input() playstate: string = "idle"; // idle | run | ko

  @ViewChild('canvasElement', { static: false }) canvasElement!: ElementRef<HTMLCanvasElement>;
  /*
  @ViewChild: Decorator to access the canvasElement in the component's template.
  'canvasElement': The local template variable name of the canvas element to be queried.
  { static: false }: Indicates that the query should be resolved after the view initialization (not during the view creation). 
  So ngAfterViewInit is required.
  ElementRef<HTMLCanvasElement>: Type of the element being accessed, wrapped in an ElementRef which provides a reference to the DOM element.
  */
  ctx!: CanvasRenderingContext2D | null; // property to hold the 2D rendering context of the canvas
  frameX: number = 0;
  frameY: number = 0;
  gameFrame: number = 0;
  
  ngOnInit(): void {
    if (this.character) {
      this.spriteSheetUrl = this.character.spriteurl;
      this.spriteHeight = this.character.spriteHeight;
      this.spriteWidth = this.character.spriteWidth;
      this.spriteAnimations = this.character.spritesheet;
    }
  }

  ngAfterViewInit(): void {
    // this.initializeSpriteAnimations();
    const canvas = this.canvasElement.nativeElement; // access the native canvas element

    if (canvas) {
      this.ctx = canvas.getContext('2d'); // obtain the 2D rendering context of the canvas

      if (this.ctx) {
        const image = new Image();
        image.src = this.spriteSheetUrl;
        // event listener to start animation once the image has loaded.
        image.onload = () => {
          this.animate(image);
        }
      }
    }
  }

  animate(image: HTMLImageElement) {
    const animateFrame = () => {
      if (this.ctx) {
        //  ↓ this clear the entire canvas to prepare for drawing the next frame
        this.ctx.clearRect(0, 0, this.canvasElement.nativeElement.width, this.canvasElement.nativeElement.height);
        // ↓ this declare the th of column of sprite frame located
        let position = Math.floor(this.gameFrame / this.staggerFrames) % this.spriteAnimations[this.playstate].coordinates.length;
        // ↓ this retrieve the coordinate of the frame from initializeSpriteAnimations() to be rendered
        this.frameX = this.spriteAnimations[this.playstate].coordinates[position].x;
        this.frameY = this.spriteAnimations[this.playstate].coordinates[position].y;

        // draw the character image with adjusted size and position
        this.ctx.drawImage(
          image, // image source
          this.frameX, // x-coordinate of the top-left corner of the sub-rectangle of the source image to draw
          this.frameY, // y-coordinate of the top-left corner of the sub-rectangle of the source image to draw.
          this.spriteWidth, // size of the rectangle of the source image to draw
          this.spriteHeight, // size of the rectangle of the source image to draw
          0, // define the top-left corner
          0, // define the top-left corner
          100, // scaled width ; better fit the eaxct size so wont waste RAM
          100 // scaled height
        );
        
        this.gameFrame++
        // console.log(this.gameFrame);
        // ↓ stop at last frame for ko animation
        if(this.playstate !== "ko" || position < this.spriteAnimations["ko"].coordinates.length - 1){
          // ↓ because previous loop do not stop and new loop start, then the animation getting lagging
          if(this.gameFrame < 3000){
            // ↓ this create a loop
            requestAnimationFrame(animateFrame);
          }
        }
      }
    };

    //  ↓ this starts the animation loop
    animateFrame();
  }
}
