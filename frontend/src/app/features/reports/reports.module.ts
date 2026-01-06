import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ReportsRoutingModule } from './reports-routing.module';
import { ReportsDashboardComponent } from './reports-dashboard/reports-dashboard.component';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    ReportsRoutingModule,
    ReportsDashboardComponent
  ]
})
export class ReportsModule { }
