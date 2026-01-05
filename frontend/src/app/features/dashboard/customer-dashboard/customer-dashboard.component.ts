import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { LoanService } from '../../../core/services/loan.service';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './customer-dashboard.component.html',
  styleUrl: './customer-dashboard.component.scss'
})
export class CustomerDashboardComponent implements OnInit {
  currentUser: any;
  myApplicationsCount = 0;
  myLoansCount = 0;
  loading = true;

  constructor(
    private authService: AuthService,
    private loanService: LoanService,
    private router: Router
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.currentUserValue;
    this.loadDashboardData();
  }

  loadDashboardData() {
    if (this.currentUser?.userId) {
      // Load user's applications
      this.loanService.getUserApplications(this.currentUser.userId).subscribe({
        next: (applications) => {
          this.myApplicationsCount = applications.length;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });

      // Load user's loans
      this.loanService.getUserLoans(this.currentUser.userId).subscribe({
        next: (loans) => {
          this.myLoansCount = loans.length;
        },
        error: () => {}
      });
    }
  }

  navigateToApplyLoan() {
    this.router.navigate(['/loans/apply']);
  }

  navigateToMyApplications() {
    this.router.navigate(['/loans/applications']);
  }

  navigateToMyLoans() {
    this.router.navigate(['/loans/my-loans']);
  }

  navigateToEMI() {
    this.router.navigate(['/emi']);
  }

  navigateToViewProfile() {
    this.router.navigate(['/view-profile']);
  }
}
