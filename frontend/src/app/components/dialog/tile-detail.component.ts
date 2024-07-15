import { Component, Inject, OnDestroy, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Property } from '../../models/property';
import { Store } from '@ngrx/store';
import { PropertyState } from '../../models/gamestate';
import { selectAllPropertyStates } from '../../state/game.selector';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-tile-detail',
  templateUrl: './tile-detail.component.html',
  styleUrl: './tile-detail.component.css'
})
export class TileDetailComponent implements OnDestroy{

  private readonly store = inject(Store);
  private destroy$ = new Subject<void>();

  isRail : boolean = false;
  isUtility : boolean = false;
  isSpecial :boolean = false;
  railID = [5, 15, 25, 35];
  uitilityId = [12, 28];
  specialTile = [5, 15, 25, 35, 12, 28];
  propertyStates !: PropertyState[];

  constructor(
    public dialogRef: MatDialogRef<TileDetailComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Property,
  ) {
    this.store.select(selectAllPropertyStates)
      .pipe(takeUntil(this.destroy$))
      .subscribe(propertyStates => {
        this.propertyStates = propertyStates;
      });

    if(this.specialTile.includes(this.data.id)){
      this.isSpecial = true;
      if(this.railID.includes(this.data.id)){
        this.isRail = true;
      }
      else {
        this.isUtility = true;
      }
    }
    else{
      this.isRail = false;
      this.isUtility = false;
      this.isSpecial = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getStars(count: number): number[] {
    return Array(count).fill(0);
  }
  
  onClick(): void {
    this.dialogRef.close();    
  }  
}
