import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { LoanService } from '../../../core/services/loan.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-loan-details',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatChipsModule, MatButtonModule],
  templateUrl: './loan-details.component.html',
  styleUrl: './loan-details.component.scss'
})
export class LoanDetailsComponent implements OnInit {
  loan: any = null;
  loading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private loanService: LoanService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'No loan ID provided.';
      this.loading = false;
      return;
    }

    this.fetchLoan(id);
  }

  private fetchLoan(id: string) {
    this.loading = true;
    this.loanService.getLoanById(id).subscribe({
      next: (loan) => {
        const currentUser = this.authService.currentUserValue;
        // Basic client-side guard: customers should only see their own loans
        if (currentUser?.role === 'CUSTOMER' && loan?.userId && loan.userId !== currentUser.userId) {
          this.error = 'You are not authorized to view this loan.';
          this.loan = null;
        } else {
          this.loan = loan;
        }
        this.loading = false;
      },
      error: (err) => {
        if (err?.status === 404) {
          this.error = 'Loan not found.';
        } else if (err?.status === 403) {
          this.error = 'You are not authorized to view this loan.';
        } else {
          this.error = err?.error?.message || 'Could not load loan details.';
        }
        this.loading = false;
      }
    });
  }

  goBack() {
    this.router.navigate(['/loans/my-loans']);
  }
}
