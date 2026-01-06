import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LoanService } from '../../../core/services/loan.service';

interface LoanView {
  id: string;
  userId: string;
  loanType: string;
  amount: number;
  termMonths: number;
  ratePercent: number;
  status: string;
  createdAt?: string;
}

@Component({
  selector: 'app-admin-active-loans',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './admin-active-loans.component.html',
  styleUrl: './admin-active-loans.component.scss'
})
export class AdminActiveLoansComponent implements OnInit {
  loans: LoanView[] = [];
  loading = false;
  error: string = '';

  constructor(private loanService: LoanService) {}

  ngOnInit(): void {
    this.loadLoans();
  }

  loadLoans(): void {
    this.loading = true;
    this.error = '';
    this.loanService.getAllLoans().subscribe({
      next: (data) => {
        const rows = data || [];
        this.loans = rows
          .filter((l: any) => (l.status || '').toUpperCase() === 'APPROVED' || (l.status || '').toUpperCase() === 'ACTIVE')
          .map((l: any) => ({
            id: l.id,
            userId: l.userId,
            loanType: l.loanType,
            amount: l.amount ?? l.principalAmount,
            termMonths: l.termMonths ?? l.tenure,
            ratePercent: l.ratePercent ?? l.interestRate,
            status: l.status,
            createdAt: l.createdAt
          }));
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || err?.message || 'Failed to load loans';
        this.loading = false;
      }
    });
  }
}
