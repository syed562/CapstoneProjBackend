import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { ProfileService } from '../../core/services/profile.service';
import { AuthService } from '../../core/services/auth.service';
import { Profile } from '../../core/models/models';

@Component({
  selector: 'app-view-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatDividerModule
  ],
  templateUrl: './view-profile.component.html',
  styleUrl: './view-profile.component.scss'
})
export class ViewProfileComponent implements OnInit {
  profile: Profile | null = null;
  loading = true;
  error = '';

  constructor(
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    const currentUser = this.authService.currentUserValue;
    
    if (!currentUser?.userId) {
      this.router.navigate(['/auth/login']);
      return;
    }

    this.profileService.getProfile(currentUser.userId).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load profile. Please complete your profile first.';
        this.loading = false;
        console.error('Profile load error:', err);
      }
    });
  }

  editProfile() {
    this.router.navigate(['/complete-profile']);
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }
}
