import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService, UpdateRateRequest } from '../../../core/services/admin.service';

@Component({
  selector: 'app-admin-loan-rules',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-loan-rules.component.html',
  styleUrl: './admin-loan-rules.component.scss'
})
export class AdminLoanRulesComponent implements OnInit {
  form!: FormGroup;
  rates: { [key: string]: number } = {};
  loanTypes = ['PERSONAL', 'HOME', 'AUTO', 'EDUCATIONAL', 'HOME_LOAN'];
  loading = false;
  submitting = false;
  editingType: string | null = null;
  error = '';
  success = '';

  displayedColumns: string[] = ['loanType', 'rate', 'actions'];

  constructor(
    private fb: FormBuilder,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadRates();
  }

  initForm(): void {
    this.form = this.fb.group({
      loanType: ['', Validators.required],
      rate: ['', [Validators.required, Validators.min(0.1), Validators.max(100)]]
    });
  }

  loadRates(): void {
    this.loading = true;
    this.error = '';
    this.success = '';
    this.adminService.getAllRates().subscribe({
      next: (rates) => {
        this.rates = rates;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load rates';
        this.loading = false;
      }
    });
  }

  startEdit(loanType: string): void {
    this.editingType = loanType;
    this.form.patchValue({
      loanType: loanType,
      rate: this.rates[loanType]
    });
  }

  cancelEdit(): void {
    this.editingType = null;
    this.form.reset();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { loanType, rate } = this.form.value;
    const request: UpdateRateRequest = {
      loanType,
      rate: parseFloat(rate)
    };

    this.submitting = true;
    this.error = '';
    this.success = '';
    this.adminService.updateRate(request).subscribe({
      next: () => {
        this.submitting = false;
        this.rates[loanType] = request.rate;
        this.form.reset();
        this.editingType = null;
        this.success = 'Rate updated successfully';
        setTimeout(() => { this.success = ''; }, 3000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to update rate';
        this.submitting = false;
      }
    });
  }

  resetToDefaults(): void {
    if (confirm('Are you sure? This will reset all rates to defaults.')) {
      this.submitting = true;
      this.error = '';
      this.success = '';
      this.adminService.resetRatesToDefaults().subscribe({
        next: (res) => {
          this.rates = res.rates;
          this.submitting = false;
          this.editingType = null;
          this.form.reset();
          this.success = 'Rates reset to defaults successfully';
          setTimeout(() => { this.success = ''; }, 3000);
        },
        error: (err) => {
          this.error = err?.error?.message || 'Failed to reset rates';
          this.submitting = false;
        }
      });
    }
  }

  get f() {
    return this.form.controls;
  }

  getRatesList(): Array<{ key: string; value: number }> {
    return Object.entries(this.rates).map(([key, value]) => ({ key, value }));
  }
}
