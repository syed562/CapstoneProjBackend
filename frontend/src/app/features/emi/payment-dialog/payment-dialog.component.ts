import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LoanService } from '../../../core/services/loan.service';

interface PaymentData {
  loanId: string;
  emiId: string;
  emiNumber: number;
  amount: number;
  dueDate: string;
}

@Component({
  selector: 'app-payment-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payment-dialog.component.html',
  styleUrl: './payment-dialog.component.scss'
})
export class PaymentDialogComponent {
  paymentAmount: number;
  paymentMethod: string = 'BANK_TRANSFER';
  transactionId: string = '';
  loading = false;
  error: string | null = null;
  success = false;

  paymentMethods = [
    { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
    { value: 'CREDIT_CARD', label: 'Credit Card' },
    { value: 'DEBIT_CARD', label: 'Debit Card' },
    { value: 'UPI', label: 'UPI' },
    { value: 'CHEQUE', label: 'Cheque' }
  ];

  constructor(
    public dialogRef: MatDialogRef<PaymentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PaymentData,
    private loanService: LoanService
  ) {
    this.paymentAmount = data.amount;
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onSubmit(): void {
    if (!this.paymentAmount || this.paymentAmount <= 0) {
      this.error = 'Please enter a valid payment amount';
      return;
    }

    if (!this.transactionId.trim()) {
      this.error = 'Please enter a transaction ID';
      return;
    }

    this.loading = true;
    this.error = null;

    // Create payment request
    const paymentRequest = {
      loanId: this.data.loanId,
      emiId: this.data.emiId,
      amount: this.paymentAmount,
      paymentMethod: this.paymentMethod,
      transactionId: this.transactionId
    };

    // Call the payment API
    this.loanService.recordPayment(paymentRequest).subscribe({
      next: (response) => {
        this.loading = false;
        this.success = true;
        setTimeout(() => {
          this.dialogRef.close(true);
        }, 1500);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Payment failed. Please try again.';
        console.error('Payment error:', err);
      }
    });
  }

  generateDummyTransactionId(): void {
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 10000);
    this.transactionId = `TXN${timestamp}${random}`;
  }
}
