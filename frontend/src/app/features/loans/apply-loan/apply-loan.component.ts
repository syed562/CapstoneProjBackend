import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
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
    MatSelectModule
  ],
  templateUrl: './apply-loan.component.html',
  styleUrl: './apply-loan.component.scss'
})
export class ApplyLoanComponent implements OnInit {
  loanForm!: FormGroup;
  loading = false;
  loanTypes = ['PERSONAL', 'HOME', 'AUTO', 'EDUCATIONAL', 'HOME_LOAN'];

  constructor(
    private fb: FormBuilder,
    private loanService: LoanService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loanForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(1000)]],
      tenure: ['', [Validators.required, Validators.min(1), Validators.max(30)]],
      loanType: ['', Validators.required],
      purpose: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loanForm.invalid) {
      return;
    }

    this.loading = true;
    const currentUser = this.authService.currentUserValue;
    
    if (!currentUser?.userId) {
      alert('Please login first');
      this.router.navigate(['/login']);
      return;
    }

    const application = {
      ...this.loanForm.value,
      userId: currentUser.userId,
      status: 'PENDING'
    };

    this.loanService.applyForLoan(application).subscribe({
      next: (response) => {
        alert('Loan application submitted successfully!');
        this.router.navigate(['/applications']);
      },
      error: (error) => {
        console.error('Error applying for loan:', error);
        alert('Failed to submit loan application');
        this.loading = false;
      }
    });
  }
}
