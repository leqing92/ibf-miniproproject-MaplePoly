import { Component, OnInit, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { ChartComponent } from '../../components/chart/chart.component';

@Component({
  selector: 'app-leaderboard',
  templateUrl: './leaderboard.component.html',
  styleUrl: './leaderboard.component.css'
})
export class LeaderboardComponent implements OnInit {

  private readonly activatedRoute = inject(ActivatedRoute);
  constructor(public dialog: MatDialog) {};

  gid !: string;

  ngOnInit(): void {
    this.gid = this.activatedRoute.snapshot.params['id'];
  }

  openLeaderBoard() {
    this.dialog.open(ChartComponent, {
      data: this.gid,
      width: '750px',
      height: '350px',
      // it apply globally, not locally https://stackoverflow.com/questions/48688614/angular-custom-style-to-mat-dialog
      // panelClass: 'custom-dialog-container',
      // backdropClass: 'custom-dialog-backdrop'
    });
  }
}
