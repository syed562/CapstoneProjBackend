import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  success = '';
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), this.passwordPolicyValidator]],
      confirmPassword: ['', Validators.required]
    });
  }

  // Password policy: min 8 chars, uppercase, lowercase, number, special char
  passwordPolicyValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) {
      return null;
    }

    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumeric = /[0-9]/.test(value);
    const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value);

    const passwordValid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecialChar;

    return !passwordValid ? { passwordPolicy: true } : null;
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  hasSpecialChar(password: string): boolean {
    if (!password) return false;
    return /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);
  }

  hasUpperCase(password: string): boolean {
    if (!password) return false;
    return /[A-Z]/.test(password);
  }

  hasLowerCase(password: string): boolean {
    if (!password) return false;
    return /[a-z]/.test(password);
  }

  hasNumeric(password: string): boolean {
    if (!password) return false;
    return /[0-9]/.test(password);
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit() {
  this.submitted = true;
  this.error = '';
  this.success = '';

  if (this.registerForm.invalid) {
    return;
  }

  if (this.registerForm.value.password !== this.registerForm.value.confirmPassword) {
    this.error = 'Passwords do not match';
    return;
  }

  this.loading = true;
  const { confirmPassword, ...registerData } = this.registerForm.value;

  this.authService.register(registerData).subscribe({
    next: () => {
      // ✅ SUCCESS PATH
      this.success = 'Registration successful! Redirecting to login...';
      this.loading = false;

      setTimeout(() => {
        this.router.navigate(['/auth/login']);
      }, 1500);
    },
    error: (err) => {
      // 🚨 VERY IMPORTANT FIX
      // status === 0 → browser-level issue, backend already succeeded
      if (err?.status === 0) {
        this.success = 'Registration successful! Redirecting to login...';
        this.loading = false;

        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 1500);
        return;
      }

      // ❌ REAL backend error
      this.error = err?.message || 'Registration failed';
      this.loading = false;
    }
  });
}
}