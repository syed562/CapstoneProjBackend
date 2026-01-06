import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './admin-users/admin-users.component';
import { AdminLoanRulesComponent } from './admin-loan-rules/admin-loan-rules.component';
import { LoanApplicationsComponent } from '../loans/loan-applications/loan-applications.component';
import { AdminActiveLoansComponent } from './admin-active-loans/admin-active-loans.component';
import { AdminLoanSchedulesComponent } from './admin-loan-schedules/admin-loan-schedules.component';
import { authGuard } from '../../core/guards/auth.guard';
import { adminGuard } from '../../core/guards/admin.guard';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    canActivate: [authGuard, adminGuard],
    component: AdminDashboardComponent
  },
  {
    path: 'users',
    canActivate: [authGuard, adminGuard],
    component: AdminUsersComponent
  },
  {
    path: 'loan-rules',
    canActivate: [authGuard, adminGuard],
    component: AdminLoanRulesComponent
  },
  {
    path: 'applications',
    canActivate: [authGuard, adminGuard],
    component: LoanApplicationsComponent
  },
  {
    path: 'active-loans',
    canActivate: [authGuard, adminGuard],
    component: AdminActiveLoansComponent
  },
  {
    path: 'loan-schedules',
    canActivate: [authGuard, adminGuard],
    component: AdminLoanSchedulesComponent
  },
  {
    path: 'reports',
    canActivate: [authGuard, adminGuard],
    loadChildren: () => import('../reports/reports.module').then(m => m.ReportsModule)
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
