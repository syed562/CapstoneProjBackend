import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoanService } from '../../../core/services/loan.service';
import { forkJoin } from 'rxjs';

interface LoanRow {
  id: string;
  userId: string;
  loanType: string;
  amount: number;
  termMonths: number;
  ratePercent: number;
  status: string;
  createdAt?: string;
  expanded?: boolean;
  schedule?: any[];
  payments?: any[];
  loadingDetails?: boolean;
}

@Component({
  selector: 'app-admin-loan-schedules',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-loan-schedules.component.html',
  styleUrl: './admin-loan-schedules.component.scss'
})
export class AdminLoanSchedulesComponent implements OnInit {
  loans: LoanRow[] = [];
  loading = false;
  error: string | null = null;

  constructor(private loanService: LoanService) {}

  ngOnInit(): void {
    this.loadLoans();
  }

  loadLoans(): void {
    this.loading = true;
    this.error = null;
    this.loanService.getAllLoans().subscribe({
      next: (data) => {
        this.loans = (data || []).map((l: any) => ({
          id: l.id,
          userId: l.userId,
          loanType: l.loanType,
          amount: l.amount ?? l.principalAmount,
          termMonths: l.termMonths ?? l.tenure,
          ratePercent: l.ratePercent ?? l.interestRate,
          status: l.status,
          createdAt: l.createdAt,
          expanded: false
        }));
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load loans';
        this.loading = false;
      }
    });
  }

  toggleDetails(loan: LoanRow): void {
    loan.expanded = !loan.expanded;
    if (loan.expanded && !loan.schedule) {
      loan.loadingDetails = true;
      forkJoin({
        schedule: this.loanService.getEMISchedule(loan.id),
        payments: this.loanService.getPaymentsByLoan(loan.id)
      }).subscribe({
        next: (res) => {
          loan.schedule = res.schedule || [];
          loan.payments = res.payments || [];
          loan.loadingDetails = false;
        },
        error: () => {
          this.error = 'Failed to load schedule/payments';
          loan.loadingDetails = false;
        }
      });
    }
  }
}
