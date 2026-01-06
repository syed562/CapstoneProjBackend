import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './admin-users/admin-users.component';
import { AdminLoanRulesComponent } from './admin-loan-rules/admin-loan-rules.component';
import { LoanApplicationsComponent } from '../loans/loan-applications/loan-applications.component';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    AdminRoutingModule,
    AdminDashboardComponent,
    AdminUsersComponent,
    AdminLoanRulesComponent,
    LoanApplicationsComponent
  ]
})
export class AdminModule { }
