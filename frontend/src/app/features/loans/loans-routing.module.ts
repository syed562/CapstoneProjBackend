import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplyLoanComponent } from './apply-loan/apply-loan.component';
import { MyLoansComponent } from './my-loans/my-loans.component';
import { LoanApplicationsComponent } from './loan-applications/loan-applications.component';
import { LoanDetailsComponent } from './loan-details/loan-details.component';
import { LoanCalculatorComponent } from './loan-calculator/loan-calculator.component';

const routes: Routes = [
  { path: 'apply', component: ApplyLoanComponent },
  { path: 'calculator', component: LoanCalculatorComponent },
  { path: 'my-loans', component: MyLoansComponent },
  { path: 'applications', component: LoanApplicationsComponent },
  { path: ':id', component: LoanDetailsComponent },
  { path: '', redirectTo: 'my-loans', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LoansRoutingModule { }
