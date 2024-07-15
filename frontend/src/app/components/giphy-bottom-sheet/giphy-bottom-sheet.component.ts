import { Component, OnInit, Output, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { GameCenterService } from '../../service/game-center.service';
import { Subject } from 'rxjs';
import { MatBottomSheetRef } from '@angular/material/bottom-sheet';

@Component({
  selector: 'app-giphy-bottom-sheet',
  templateUrl: './giphy-bottom-sheet.component.html',
  styleUrl: './giphy-bottom-sheet.component.css'
})
export class GiphyBottomSheetComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly httpSvc = inject(GameCenterService);

  constructor(private _bottomSheetRef: MatBottomSheetRef<GiphyBottomSheetComponent>) {}
  
  form !: FormGroup;
  giphys !: string[];
  searching !: boolean;
  found : boolean = true;
  @Output() giphySelected = new Subject<string>();

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      item : this.formBuilder.control<string>("", [Validators.required])      
    })
  } 

  search() : void {
    this.found = true;
    this.searching = true;
    const q : string = this.form.get('item')?.value;
    this.httpSvc.giphysearch(q)
      .then(resp =>{
        this.giphys = resp;
        this.searching = false;        
      })
      .catch(err =>{
        this.giphys= [];
        this.found = false
        this.searching = false;
      });
  }

  submit(i : number) : void {
    this._bottomSheetRef.dismiss();
    this.giphySelected.next(this.giphys[i]);
  }

}
