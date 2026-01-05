import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    const user = this.authService.currentUserValue;
    
    if (!user) {
      this.router.navigate(['/auth/login']);
      return;
    }

    // Route based on user role
    switch (user.role) {
      case 'CUSTOMER':
        this.router.navigate(['/customer/dashboard']);
        break;
      case 'LOAN_OFFICER':
        this.router.navigate(['/officer/dashboard']);
        break;
      case 'ADMIN':
        this.router.navigate(['/admin/dashboard']);
        break;
      default:
        this.router.navigate(['/auth/login']);
    }
  }
}
