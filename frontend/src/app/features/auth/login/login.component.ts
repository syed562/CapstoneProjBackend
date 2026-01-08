import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ProfileService } from '../../../core/services/profile.service';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
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
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  showPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private profileService: ProfileService
  ) {}

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit() {
    this.submitted = true;
    this.error = '';

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        const currentUser = this.authService.currentUserValue;

        // Role-based routing
        if (currentUser?.role === 'ADMIN') {
          // Admin goes directly to admin dashboard
          this.router.navigate(['/admin']);
        } else if (currentUser?.role === 'LOAN_OFFICER') {
          // Loan Officer goes directly to their dashboard
          this.router.navigate(['/officer']);
        } else {
          // Customer needs to complete profile first
          if (currentUser?.userId) {
            this.profileService.getProfile(currentUser.userId).subscribe({
              next: (profile) => {
                // Profile exists, go to dashboard
                this.router.navigate(['/dashboard']);
              },
              error: () => {
                // Profile doesn't exist, redirect to complete profile
                this.router.navigate(['/complete-profile']);
              }
            });
          } else {
            this.router.navigate(['/dashboard']);
          }
        }
      },
      error: (err) => {
        const errorMessage = err?.error?.message || err?.message || 'Login failed';
        
        // Check for specific error messages related to inactive/deactivated users
        if (errorMessage.toLowerCase().includes('inactive') || 
            errorMessage.toLowerCase().includes('deactivated') ||
            errorMessage.toLowerCase().includes('disabled')) {
          this.error = 'Your account has been deactivated. Please contact the administrator for assistance.';
        } else if (errorMessage.toLowerCase().includes('invalid credentials') || 
                   errorMessage.toLowerCase().includes('bad credentials') ||
                   errorMessage.toLowerCase().includes('unauthorized')) {
          this.error = 'Invalid username or password. Please try again.';
        } else {
          this.error = errorMessage;
        }
        
        this.loading = false;
      }
    });
  }
}
