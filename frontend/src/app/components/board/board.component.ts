import { ChangeDetectorRef, Component, Input, OnDestroy, OnInit, inject} from '@angular/core';
import { Property } from '../../models/property';
import { MatDialog } from '@angular/material/dialog';
import { TileDetailComponent } from '../dialog/tile-detail.component';
import { Character } from '../../models/character';
import { Subject, combineLatest, distinctUntilChanged, takeUntil } from 'rxjs';
import { Store } from '@ngrx/store';
import { selectAllPlayerStates, selectAllPropertyStates } from '../../state/game.selector';
import { PlayerState, PropertyState } from '../../models/gamestate';

@Component({
  selector: 'app-board',
  templateUrl: './board.component.html',
  styleUrl: './board.component.css'
})
export class BoardComponent implements OnInit, OnDestroy{

  private readonly store = inject(Store);
  private readonly cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  playerStates ?: PlayerState[];
  propertyStates ?: PropertyState[];

  @Input() properties !: Property[];
  @Input() characters!: Character[];  
  
  ngOnInit(): void {
    this.subscribeToGameState();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  subscribeToGameState() : void {
    const playerState$ = this.store.select(selectAllPlayerStates);
    const propertyStates$ = this.store.select(selectAllPropertyStates);

    combineLatest([playerState$, propertyStates$])
      .pipe(
        takeUntil(this.destroy$),
        // seem no use?
        distinctUntilChanged(([prevPlayerStates, prevPropertyStates], [currPlayerStates, currPropertyStates]) => {
          // check for changes in playerStates or propertyStates
          return JSON.stringify(prevPlayerStates) === JSON.stringify(currPlayerStates) &&
                 JSON.stringify(prevPropertyStates) === JSON.stringify(currPropertyStates);
        })
      )
      .subscribe({
        next: ([playerStates, propertyStates]) => {
          this.playerStates = playerStates;
          this.propertyStates = propertyStates;
          this.cdr.detectChanges();
        },
        error: error => {
          alert(error);
        }
      });
  }

  // for remove peek detail on special tiles
  isSpecialTile(index: number): boolean {
    return [0, 2, 4, 7, 10, 17, 20, 22, 30, 33, 36, 38, 40].includes(index);
  }

  owner(id : number) : string{    
    if(this.propertyStates && this.playerStates){
      const propertyState = this.propertyStates[id];
    
      // find the corresponding playerState based on owner name
      const playerState = this.playerStates.find(player => player.name === propertyState.owner);

      if(playerState){
        const character = this.characters.find(c => c.name === playerState.character);
        
        if(character){
          return character.url;
        }
      }
    }
    return "";
  }

  // for make house image
  getStars(count: number): number[] {
    return Array(count).fill(0);
  }
  
  // for the property info on click
  constructor(public dialog: MatDialog) {};
  openDialog(index : number) {
    this.dialog.open(TileDetailComponent, {
      data: this.properties[index],
      width: '250px',
      height: '300px'
    });
  }
}
