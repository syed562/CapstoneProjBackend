import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatSliderModule } from '@angular/material/slider';

@Component({
  selector: 'app-loan-calculator',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatSelectModule,
    MatIconModule,
    MatSliderModule
  ],
  templateUrl: './loan-calculator.component.html',
  styleUrl: './loan-calculator.component.scss'
})
export class LoanCalculatorComponent implements OnInit {
  calculatorForm!: FormGroup;
  emiResult: any = null;

  // Predefined rates by loan type
  loanRates: { [key: string]: number } = {
    PERSONAL: 10.5,
    HOME: 6.5,
    AUTO: 8.0,
    EDUCATIONAL: 7.5,
    HOME_LOAN: 6.5
  };

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.calculatorForm = this.fb.group({
      loanType: ['PERSONAL', Validators.required],
      amount: [500000, [Validators.required, Validators.min(10000)]],
      tenure: [12, [Validators.required, Validators.min(6), Validators.max(360)]],
      rate: [10.5, [Validators.required, Validators.min(0.1)]]
    });

    // Update rate when loan type changes
    this.calculatorForm.get('loanType')?.valueChanges.subscribe((type) => {
      const rate = this.loanRates[type];
      this.calculatorForm.patchValue({ rate }, { emitEvent: false });
      this.calculate();
    });

    // Recalculate on other changes
    this.calculatorForm.get('amount')?.valueChanges.subscribe(() => this.calculate());
    this.calculatorForm.get('tenure')?.valueChanges.subscribe(() => this.calculate());
    this.calculatorForm.get('rate')?.valueChanges.subscribe(() => this.calculate());

    // Initial calculation
    this.calculate();
  }

  calculate() {
    const { amount, tenure, rate } = this.calculatorForm.value;

    if (!amount || !tenure || !rate) return;

    const monthlyRate = rate / 100 / 12;
    const emi =
      (amount * monthlyRate * Math.pow(1 + monthlyRate, tenure)) /
      (Math.pow(1 + monthlyRate, tenure) - 1);

    const totalAmount = emi * tenure;
    const totalInterest = totalAmount - amount;

    this.emiResult = {
      monthlyEMI: emi.toFixed(2),
      totalAmount: totalAmount.toFixed(2),
      totalInterest: totalInterest.toFixed(2),
      principalAmount: amount.toFixed(2),
      rate: rate.toFixed(2)
    };
  }

  reset() {
    this.calculatorForm.reset({
      loanType: 'PERSONAL',
      amount: 500000,
      tenure: 12,
      rate: this.loanRates['PERSONAL']
    });
    this.calculate();
  }
}
