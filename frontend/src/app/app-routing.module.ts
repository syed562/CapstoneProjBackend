import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { customerGuard } from './core/guards/customer.guard';
import { loanOfficerGuard } from './core/guards/loan-officer.guard';
import { CompleteProfileComponent } from './features/profile/complete-profile.component';
import { ViewProfileComponent } from './features/profile/view-profile.component';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'complete-profile',
    canActivate: [authGuard],
    component: CompleteProfileComponent
  },
  {
    path: 'view-profile',
    canActivate: [authGuard],
    component: ViewProfileComponent
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule)
  },
  {
    path: 'customer',
    canActivate: [authGuard, customerGuard],
    loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule)
  },
  {
    path: 'officer',
    canActivate: [authGuard, loanOfficerGuard],
    loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule)
  },
  {
    path: 'loans',
    canActivate: [authGuard],
    loadChildren: () => import('./features/loans/loans.module').then(m => m.LoansModule)
  },
  {
    path: 'emi',
    canActivate: [authGuard],
    loadChildren: () => import('./features/emi/emi.module').then(m => m.EmiModule)
  },
  {
    path: 'reports',
    canActivate: [authGuard],
    loadChildren: () => import('./features/reports/reports.module').then(m => m.ReportsModule)
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadChildren: () => import('./features/admin/admin.module').then(m => m.AdminModule)
  },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
