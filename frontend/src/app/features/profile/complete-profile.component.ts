import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatStepperModule } from '@angular/material/stepper';
import { MatIconModule } from '@angular/material/icon';
import { ProfileService } from '../../core/services/profile.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-complete-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatStepperModule,
    MatIconModule
  ],
  templateUrl: './complete-profile.component.html',
  styleUrl: './complete-profile.component.scss'
})
export class CompleteProfileComponent implements OnInit {
  personalInfoForm!: FormGroup;
  addressForm!: FormGroup;
  financialForm!: FormGroup;
  loading = false;
  error = '';
  success = '';
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Personal Information Form
    this.personalInfoForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]]
    });

    // Address Form
    this.addressForm = this.fb.group({
      addressLine1: ['', Validators.required],
      addressLine2: [''],
      city: ['', Validators.required],
      state: ['', Validators.required],
      postalCode: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      country: ['India', Validators.required]
    });

    // Financial Information Form
    this.financialForm = this.fb.group({
      annualIncome: ['', [Validators.required, Validators.min(0)]],
      creditScore: ['', [Validators.min(300), Validators.max(900)]],
      totalLiabilities: ['', [Validators.min(0)]]
    });

    // Pre-fill email from user account
    const currentUser = this.authService.currentUserValue;
    if (currentUser?.email) {
      this.personalInfoForm.patchValue({ email: currentUser.email });
    }
  }

  onSubmit() {
    this.submitted = true;
    this.error = '';
    this.success = '';
    
    if (this.personalInfoForm.invalid || this.addressForm.invalid || this.financialForm.invalid) {
      return;
    }

    this.loading = true;
    const currentUser = this.authService.currentUserValue;
    
    if (!currentUser?.userId) {
      this.error = 'Please login first';
      this.loading = false;
      this.router.navigate(['/login']);
      return;
    }

    const profile = {
      userId: currentUser.userId,
      ...this.personalInfoForm.value,
      ...this.addressForm.value,
      ...this.financialForm.value,
      kycStatus: 'PENDING'
    };

    this.profileService.createProfile(profile, currentUser.userId).subscribe({
      next: (response) => {
        // Update user object with profile completed flag
        const updatedUser = { ...currentUser, profileCompleted: true };
        localStorage.setItem('user', JSON.stringify(updatedUser));
        
        this.success = 'Profile completed successfully!';
        this.loading = false;
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 1500);
      },
      error: (error) => {
        this.error = error?.error?.message || error?.message || 'Failed to create profile. Please try again.';
        this.loading = false;
        console.error('Error creating profile:', error);
      }
    });
  }

  skipForNow() {
    if (confirm('You can complete your profile later, but some features may be limited. Continue?')) {
      this.router.navigate(['/dashboard']);
    }
  }
}
