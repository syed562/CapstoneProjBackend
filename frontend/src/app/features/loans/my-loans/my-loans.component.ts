import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { LoanService } from '../../../core/services/loan.service';
import { AuthService } from '../../../core/services/auth.service';

interface LoanView {
  id: string;
  amount: number;
  termMonths: number;
  status: string;
  loanType: string;
  ratePercent?: number;
  createdAt?: string;
}

@Component({
  selector: 'app-my-loans',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './my-loans.component.html',
  styleUrl: './my-loans.component.scss'
})
export class MyLoansComponent implements OnInit {
  loans: LoanView[] = [];
  loading = false;
  error: string = '';

  constructor(
    private loanService: LoanService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (!user?.userId) {
      this.router.navigate(['/auth/login']);
      return;
    }

    this.loading = true;
    this.loanService.getUserLoans(user.userId).subscribe({
      next: (loans) => {
        this.loans = (loans || []).map((loan: any) => ({
          id: loan.id,
          amount: loan.amount,
          termMonths: loan.termMonths ?? loan.tenure,
          status: loan.status,
          loanType: loan.loanType,
          ratePercent: loan.ratePercent ?? loan.interestRate,
          createdAt: loan.createdAt ?? loan.startDate
        }));
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || err?.message || 'Could not load your loans right now.';
        this.loading = false;
        console.error('Failed to load loans', err);
      }
    });
  }
}
