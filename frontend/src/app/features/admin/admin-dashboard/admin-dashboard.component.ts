import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  currentUser: any;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.currentUserValue;
  }

  navigateToUsers() {
    this.router.navigate(['/admin/users']);
  }

  navigateToLoanRules() {
    this.router.navigate(['/admin/loan-rules']);
  }

  navigateToReports() {
    this.router.navigate(['/admin/reports']);
  }

  navigateToApplications() {
    this.router.navigate(['/admin/applications']);
  }
}
