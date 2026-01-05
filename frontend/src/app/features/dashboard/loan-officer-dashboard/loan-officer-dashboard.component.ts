import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../core/services/auth.service';
import { LoanService } from '../../../core/services/loan.service';

@Component({
  selector: 'app-loan-officer-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule
  ],
  templateUrl: './loan-officer-dashboard.component.html',
  styleUrl: './loan-officer-dashboard.component.scss'
})
export class LoanOfficerDashboardComponent implements OnInit {
  currentUser: any;
  pendingApplicationsCount = 0;
  approvedApplicationsCount = 0;
  rejectedApplicationsCount = 0;
  recentApplications: any[] = [];
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
    this.loanService.getAllApplications().subscribe({
      next: (applications) => {
        this.pendingApplicationsCount = applications.filter(app => app.status === 'PENDING').length;
        this.approvedApplicationsCount = applications.filter(app => app.status === 'APPROVED').length;
        this.rejectedApplicationsCount = applications.filter(app => app.status === 'REJECTED').length;
        this.recentApplications = applications.slice(0, 5);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  navigateToPendingApplications() {
    this.router.navigate(['/loans/applications'], { queryParams: { status: 'PENDING' } });
  }

  navigateToAllApplications() {
    this.router.navigate(['/loans/applications']);
  }

  navigateToReports() {
    this.router.navigate(['/reports']);
  }

  navigateToRepayments() {
    this.router.navigate(['/loans/my-loans']);
  }

  viewApplication(applicationId: string) {
    this.router.navigate(['/loans', applicationId]);
  }
}
