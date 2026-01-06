import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoanService } from '../../../core/services/loan.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { PaymentDialogComponent } from '../payment-dialog/payment-dialog.component';

interface EMIRow {
  emiId?: string;
  loanId?: string;
  emiNumber: number;
  dueDate: string;
  amount: number;
  principal: number;
  interest: number;
  remainingBalance: number;
  status: string;
  paidDate?: string;
}

@Component({
  selector: 'app-emi-schedule',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatDialogModule
  ],
  templateUrl: './emi-schedule.component.html',
  styleUrl: './emi-schedule.component.scss'
})
export class EmiScheduleComponent implements OnInit {
  schedule: EMIRow[] = [];
  loading = false;
  error: string | null = null;
  currentLoanId: string = '';
  displayedColumns: string[] = ['emiNumber', 'dueDate', 'amount', 'principal', 'interest', 'remainingBalance', 'status', 'actions'];

  constructor(
    private loanService: LoanService,
    private authService: AuthService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (!user?.userId) {
      this.router.navigate(['/auth/login']);
      return;
    }

    // Load all loans and get EMI schedules for each
    this.loadEMISchedules(user.userId);
  }

  private loadEMISchedules(userId: string): void {
    this.loading = true;
    this.loanService.getUserLoans(userId).subscribe({
      next: (loans) => {
        if (loans && loans.length > 0) {
          // Get EMI schedule for the first active loan
          const activeLoan = loans.find((l: any) => l.status === 'ACTIVE' || l.status === 'APPROVED');
          if (activeLoan) {
            this.fetchEMISchedule(activeLoan.id);
          } else {
            this.error = 'No active loans found';
            this.loading = false;
          }
        } else {
          this.error = 'No loans found';
          this.loading = false;
        }
      },
      error: (err) => {
        this.error = 'Failed to load loans';
        this.loading = false;
        console.error(err);
      }
    });
  }

  private fetchEMISchedule(loanId: string): void {
    this.currentLoanId = loanId;
    this.loanService.getEMISchedule(loanId).subscribe({
      next: (schedule) => {
        this.schedule = (schedule || []).map((item: any) => ({
          emiId: item.id || item.emiId,
          loanId: loanId,
          emiNumber: item.installmentNumber || item.emiNumber,
          dueDate: item.dueDate,
          amount: item.emiAmount || item.amount,
          principal: item.principalComponent || item.principal || 0,
          interest: item.interestComponent || item.interest || 0,
          remainingBalance: item.outstandingBalance || item.remainingBalance || 0,
          status: item.status,
          paidDate: item.paidAt || item.paidDate
        }));
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load EMI schedule';
        this.loading = false;
        console.error(err);
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status?.toLowerCase()) {
      case 'paid':
        return 'accent';
      case 'scheduled':
        return 'primary';
      case 'overdue':
        return 'warn';
      default:
        return '';
    }
  }

  openPaymentDialog(row: EMIRow): void {
    const dialogRef = this.dialog.open(PaymentDialogComponent, {
      width: '400px',
      data: {
        loanId: this.currentLoanId,
        emiId: row.emiId,
        emiNumber: row.emiNumber,
        amount: row.amount,
        dueDate: row.dueDate
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.fetchEMISchedule(this.currentLoanId);
      }
    });
  }

  getPaidCount(): number {
    return this.schedule.filter(x => x.status?.toLowerCase() === 'paid').length;
  }

  getPendingCount(): number {
    return this.schedule.filter(x => x.status?.toLowerCase() !== 'paid').length;
  }

  getTotalAmount(): number {
    return this.schedule.reduce((sum, x) => sum + x.amount, 0);
  }
}
