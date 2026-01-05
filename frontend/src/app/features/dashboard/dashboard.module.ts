import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CustomerDashboardComponent } from './customer-dashboard/customer-dashboard.component';
import { LoanOfficerDashboardComponent } from './loan-officer-dashboard/loan-officer-dashboard.component';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    DashboardComponent,
    CustomerDashboardComponent,
    LoanOfficerDashboardComponent
  ]
})
export class DashboardModule { }
