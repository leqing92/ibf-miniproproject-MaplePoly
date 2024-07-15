import { Component, Inject, Optional, inject } from '@angular/core';
// import { Chart } from 'chart.js';
import { Chart } from 'chart.js/auto';
import { GameService } from '../../service/game.service';
import { ChartData, DataSet } from '../../models/chart';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrl: './chart.component.css'
})
// https://www.chartjs.org/docs/latest/
// https://www.npmjs.com/package/chart.js?activeTab=readme
// https://www.freecodecamp.org/news/how-to-make-bar-and-line-charts-using-chartjs-in-angular/
export class ChartComponent {

  private readonly gameSvc = inject(GameService);
  private readonly activatedRoute = inject(ActivatedRoute);

  public chart: any;
  gid !: string;
  chartData !: ChartData;
  colors: string[] = ['blue', 'limegreen', 'red', 'purple'];
  selectedDataType: string = 'total';

  // allow to open as maat-ialog and normal component by Optional()
  constructor(
    @Optional() public dialogRef: MatDialogRef<ChartComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: string,
  ) {
    if (this.data) {
      this.gid = this.data;
    }
  }

  ngOnInit(): void {
    if (!this.gid) {
      this.gid = this.activatedRoute.snapshot.params["id"];
    }
    this.gameSvc.getGameChartInfo(this.gid)
      .then(resp => {
        this.chartData = resp;
        this.createChart(this.chartData.labels, this.chartData.datasets);
      })
    
  } 

  createChart(labels : number[], datasets : DataSet[]){  
    this.chart = new Chart("MyChart", {
      type : 'line', //this denotes tha type of chart

      data : {
        // values on X-Axis
        labels : labels, 
        // values on y-axis
        datasets : this.chartData.datasets.map((dataset, index) => ({
          label : dataset.label,
          data : dataset.total,
          backgroundColor: this.colors[index], //dot color
          borderColor: this.colors[index], //line color
          // fill: false, //area under graph
          
        }))
      },

      options : {
        aspectRatio : 2.5,
        scales : {
          x : {
            title : {
              display : true,
              text : 'Turn',
              color : "black"
            },
            ticks: {
              color: 'black' // axis label color
            }
          },
          y : {
            title : {
              display : true,
              text : 'Money',
              color : "black"
            },
            ticks: {
              color: 'black' // axis label color
            }
          }
        }
      }
      
    });
  }

  updateChart(): void {
    if (this.chart) {
      this.chart.data.datasets.forEach(
        (dataset: { data: number[]; }, index: number) => {
          dataset.data = this.chartData.datasets[index][this.selectedDataType as keyof DataSet] as number[];
        });      
      this.chart.update();
    }
  }
  
}


