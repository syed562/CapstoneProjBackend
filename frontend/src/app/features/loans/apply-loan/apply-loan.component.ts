import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { LoanService } from '../../../core/services/loan.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-apply-loan',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './apply-loan.component.html',
  styleUrl: './apply-loan.component.scss'
})
export class ApplyLoanComponent implements OnInit {
  loanForm!: FormGroup;
  loading = false;
  error = '';
  success = '';
  submitted = false;
  loanTypes = ['PERSONAL', 'HOME', 'AUTO', 'EDUCATIONAL', 'HOME_LOAN'];

  constructor(
    private fb: FormBuilder,
    private loanService: LoanService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loanForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(10000)]],
      termMonths: ['', [Validators.required, Validators.min(6), Validators.max(360)]],
      loanType: ['', Validators.required],
      ratePercent: ['']
    });
  }

  onSubmit() {
    this.submitted = true;
    this.error = '';
    this.success = '';
    
    if (this.loanForm.invalid) {
      return;
    }

    this.loading = true;
    const currentUser = this.authService.currentUserValue;
    
    if (!currentUser?.userId) {
      this.error = 'Please login first';
      this.loading = false;
      return;
    }

    // Submit form values directly - they match ApplyRequest DTO
    const application = this.loanForm.value;

    this.loanService.applyForLoan(application).subscribe({
      next: (response) => {
        this.success = 'Loan application submitted successfully!';
        this.loading = false;
        setTimeout(() => {
          this.router.navigate(['/loans/applications']);
        }, 1500);
      },
      error: (error) => {
        this.error = error?.error?.message || error?.message || 'Failed to submit loan application. Please try again.';
        this.loading = false;
        console.error('Error applying for loan:', error);
      }
    });
  }
}
