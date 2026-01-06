import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoanService } from '../../../core/services/loan.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';

interface EMIRow {
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
  imports: [CommonModule],
  templateUrl: './emi-schedule.component.html',
  styleUrl: './emi-schedule.component.scss'
})
export class EmiScheduleComponent implements OnInit {
  schedule: EMIRow[] = [];
  loading = false;
  error: string | null = null;

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

    // TODO: pick a loanId to fetch EMI schedule. For now, no-op until UI passes loanId.
  }
}
