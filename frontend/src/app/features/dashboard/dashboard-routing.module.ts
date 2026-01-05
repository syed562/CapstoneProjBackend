import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CustomerDashboardComponent } from './customer-dashboard/customer-dashboard.component';
import { LoanOfficerDashboardComponent } from './loan-officer-dashboard/loan-officer-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent
  },
  {
    path: 'dashboard',
    component: CustomerDashboardComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }
