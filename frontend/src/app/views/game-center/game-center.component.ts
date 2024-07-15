import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { GameCenterService } from '../../service/game-center.service';
import { GameRoom } from '../../models/gameroom';
import { Subject} from 'rxjs';
import { Store } from '@ngrx/store';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { AuthService } from '../../service/authentication.service';
import { UserState } from '../../models/user';

@Component({
  selector: 'app-game-center',
  templateUrl: './game-center.component.html',
  styleUrl: './game-center.component.css'
})
export class GameCenterComponent implements OnInit, OnDestroy{
  
  private readonly router = inject(Router);
  private readonly gameCenterSvc = inject(GameCenterService);  
  private readonly userStore = inject(Store);
  private readonly authSvc = inject(AuthService);
  private destroy$ = new Subject<void>();
  
  gameRooms !: GameRoom[];
  name !: string;
  sortedData !: GameRoom[];
  headers : string[] = ['gid', "owner", "players", "action"];
  dataSource !: MatTableDataSource<GameRoom>;
  storename !: UserState;
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  
  ngOnInit(): void {
    this.gameCenterSvc.getGameRoomList()
      .subscribe({
        next: (resp: GameRoom[]) => {
          this.gameRooms = resp;

          // for filter 
          this.dataSource = new MatTableDataSource(this.gameRooms);
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;

          // for remove players name in the list from filter search
          this.dataSource.filterPredicate = (data: GameRoom, filter: string) => {
            const dataStr = data.gid + ' ' + data.owner + ' ' + data.players.length;
            return dataStr.toLowerCase().includes(filter.trim().toLowerCase());
          };
        },
        error: (err: any) => {
          console.error(err);
        }
      })

    this.name = localStorage.getItem("username") || "";

    // this.userStore.select(selectUsername)
    //   .pipe(takeUntil(this.destroy$))
    //   .subscribe({
    //     next: resp => {
    //       if(!resp){
    //         this.authSvc.reloadDatatoStore()
    //         .then(resp => {
    //           this.storename = resp;
    //           console.log("username:", this.storename);
    //         })
    //       }
          
    //     }
    //   });    
  }

  ngAfterViewInit() {
    if (this.dataSource) {
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  join(gid: string, owner : string) {
    this.gameCenterSvc.joinGameRoom(gid, this.name)
      .then(resp =>{
        this.router.navigate([`/gameroom/${gid}`]);
      })
      .catch(error => {
        if (error.status === 404) {          
          alert('Game room not found. The game might had started / deleted');
        } 
        else {          
          alert(JSON.stringify(error.error));
        }
        location.reload();
      });
        
  }

  isFull(players : number) : boolean {
    if(players == 4){
      return true
    }
    return false;
  }

  create() {
    this.gameCenterSvc.createGameRoom(this.name)
      .then((resp: { gid: string; })=>{
        const gid : string = resp.gid;
        // console.log("gid:", gid);
        this.router.navigate([`/gameroom/${gid}`]);
      })
      .catch((err: any) => alert(JSON.stringify(err.error)));
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }
  
  // sortData(sort: Sort) {
  //   const data = this.gameRooms.slice();
  //   if (!sort.active || sort.direction === '') {
  //     this.sortedData = data;
  //     return;
  //   }

  //   this.sortedData = data.sort((a, b) => {
  //     const isAsc = sort.direction === 'asc';
  //     switch (sort.active) {
  //       case 'gid':
  //         return compare(a.gid, b.gid, isAsc);
  //       case 'owner':
  //         return compare(a.owner, b.owner, isAsc);
  //       case 'players':
  //         return compare(a.players.length, b.players.length, isAsc);     
  //       default:
  //         return 0;
  //     }
  //   });
  // }
  
}

// function compare(a: number | string, b: number | string, isAsc: boolean) {
//   return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
// }
