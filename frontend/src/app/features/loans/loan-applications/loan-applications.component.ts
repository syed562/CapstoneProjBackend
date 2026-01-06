import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { LoanService } from '../../../core/services/loan.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserAdminService } from '../../../core/services/user-admin.service';

interface ApplicationView {
  id: string;
  userId: string;
  userName?: string;
  amount: number;
  termMonths: number;
  status: string;
  loanType: string;
  createdAt?: string;
  actionInProgress?: boolean;
}

@Component({
  selector: 'app-loan-applications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './loan-applications.component.html',
  styleUrl: './loan-applications.component.scss'
})
export class LoanApplicationsComponent implements OnInit {
  applications: ApplicationView[] = [];
  loading = false;
  error: string | null = null;
  currentUserRole: string = '';

  constructor(
    private loanService: LoanService,
    private authService: AuthService,
    private userAdminService: UserAdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (!user?.userId) {
      this.router.navigate(['/auth/login']);
      return;
    }

    this.currentUserRole = user.role || '';
    this.loading = true;
    const isAdminOrOfficer = user.role === 'ADMIN' || user.role === 'LOAN_OFFICER';
    const request$ = isAdminOrOfficer
      ? this.loanService.getAllApplications()
      : this.loanService.getUserApplications(user.userId);

    request$.subscribe({
      next: (apps) => {
        // Create a map of applications
        const applicationMap = new Map<string, any>();
        (apps || []).forEach((a: any) => {
          applicationMap.set(a.userId, {
            id: a.id,
            userId: a.userId,
            userName: a.userName,
            amount: a.amount,
            termMonths: a.termMonths ?? a.tenure,
            status: a.status,
            loanType: a.loanType,
            createdAt: a.createdAt,
            actionInProgress: false
          });
        });

        // If admin/officer and applications don't have userNames, fetch user details
        if (isAdminOrOfficer && (apps || []).some((a: any) => !a.userName)) {
          const userIds = Array.from(new Set((apps || []).map((a: any) => a.userId)));
          this.userAdminService.listUsers().subscribe({
            next: (users) => {
              const userMap = new Map();
              (users || []).forEach((u: any) => {
                userMap.set(u.id, u.username);
              });

              this.applications = (apps || []).map((a: any) => ({
                id: a.id,
                userId: a.userId,
                userName: a.userName || userMap.get(a.userId) || 'Unknown',
                amount: a.amount,
                termMonths: a.termMonths ?? a.tenure,
                status: a.status,
                loanType: a.loanType,
                createdAt: a.createdAt,
                actionInProgress: false
              }));
              this.loading = false;
            },
            error: (err) => {
              console.error('Failed to load user details', err);
              // Fallback: show applications without user details
              this.applications = (apps || []).map((a: any) => ({
                id: a.id,
                userId: a.userId,
                userName: a.userName || 'Unknown',
                amount: a.amount,
                termMonths: a.termMonths ?? a.tenure,
                status: a.status,
                loanType: a.loanType,
                createdAt: a.createdAt,
                actionInProgress: false
              }));
              this.loading = false;
            }
          });
        } else {
          this.applications = (apps || []).map((a: any) => ({
            id: a.id,
            userId: a.userId,
            userName: a.userName || 'Unknown',
            amount: a.amount,
            termMonths: a.termMonths ?? a.tenure,
            status: a.status,
            loanType: a.loanType,
            createdAt: a.createdAt,
            actionInProgress: false
          }));
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('Failed to load applications', err);
        this.error = 'Could not load applications right now.';
        this.loading = false;
      }
    });
  }

  isAdminOrOfficer(): boolean {
    return this.currentUserRole === 'ADMIN' || this.currentUserRole === 'LOAN_OFFICER';
  }

  approve(application: ApplicationView): void {
    if (!confirm(`Approve loan application from ${application.userName || 'User'} for ₹${application.amount}?`)) {
      return;
    }
    application.actionInProgress = true;
    this.loanService.approveApplication(application.id).subscribe({
      next: () => {
        application.status = 'APPROVED';
        application.actionInProgress = false;
        alert('Application approved successfully!');
      },
      error: (err) => {
        application.actionInProgress = false;
        console.error('Failed to approve application', err);
        alert('Failed to approve application: ' + (err?.error?.message || err?.message));
      }
    });
  }

  moveToReview(application: ApplicationView): void {
    if (!confirm(`Move application from ${application.userName || 'User'} to UNDER_REVIEW?`)) {
      return;
    }
    application.actionInProgress = true;
    this.loanService.markUnderReview(application.id).subscribe({
      next: () => {
        application.status = 'UNDER_REVIEW';
        application.actionInProgress = false;
      },
      error: (err) => {
        application.actionInProgress = false;
        console.error('Failed to move application to UNDER_REVIEW', err);
        alert('Failed to mark as under review: ' + (err?.error?.message || err?.message));
      }
    });
  }

  reject(application: ApplicationView): void {
    const reason = prompt('Enter reason for rejection:');
    if (!reason) return;

    application.actionInProgress = true;
    this.loanService.rejectApplication(application.id, reason).subscribe({
      next: () => {
        application.status = 'REJECTED';
        application.actionInProgress = false;
        alert('Application rejected successfully!');
      },
      error: (err) => {
        application.actionInProgress = false;
        console.error('Failed to reject application', err);
        alert('Failed to reject application: ' + (err?.error?.message || err?.message));
      }
    });
  }
}
