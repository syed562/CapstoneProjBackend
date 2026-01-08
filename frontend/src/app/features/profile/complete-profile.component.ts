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
  currentUser: any = null;

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.currentUserValue;

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

    // Lock down email and username (email as form control, username displayed only)
    if (this.currentUser?.email) {
      this.personalInfoForm.patchValue({ email: this.currentUser.email });
      this.personalInfoForm.get('email')?.disable();
    }

    // Prefill profile data if it exists
    if (this.currentUser?.userId) {
      this.profileService.getProfile(this.currentUser.userId).subscribe({
        next: (profile) => {
          this.patchProfile(profile);
          if (profile?.email) {
            this.personalInfoForm.patchValue({ email: profile.email });
            this.personalInfoForm.get('email')?.disable();
          }
        },
        error: () => {
          // if profile missing, stay in empty state
        }
      });
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
    if (!this.currentUser?.userId) {
      this.error = 'Please login first';
      this.loading = false;
      this.router.navigate(['/login']);
      return;
    }

    const profile = {
      userId: this.currentUser.userId,
      username: this.currentUser.username,
      ...this.personalInfoForm.getRawValue(),
      ...this.addressForm.value,
      ...this.financialForm.value,
      kycStatus: 'PENDING'
    };

    this.profileService.createProfile(profile, this.currentUser.userId).subscribe({
      next: (response) => {
        // Update user object with profile completed flag
        const updatedUser = { ...this.currentUser, profileCompleted: true };
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

  private patchProfile(profile: any) {
    if (!profile) return;

    this.personalInfoForm.patchValue({
      firstName: profile.firstName || '',
      lastName: profile.lastName || '',
      email: this.currentUser?.email || profile.email || '',
      phone: profile.phone || ''
    });

    this.addressForm.patchValue({
      addressLine1: profile.addressLine1 || '',
      addressLine2: profile.addressLine2 || '',
      city: profile.city || '',
      state: profile.state || '',
      postalCode: profile.postalCode || '',
      country: profile.country || 'India'
    });

    this.financialForm.patchValue({
      annualIncome: profile.annualIncome ?? '',
      creditScore: profile.creditScore ?? '',
      totalLiabilities: profile.totalLiabilities ?? ''
    });
  }
}
