import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard to prevent Admin and Loan Officer from completing profile
 * They have their own dashboards and don't need profiles
 */
export const profileCompletionGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.currentUserValue;

  // Only CUSTOMER role should access profile completion
  if (currentUser && currentUser.role === 'CUSTOMER') {
    return true;
  }

  // Admin goes to admin dashboard
  if (currentUser?.role === 'ADMIN') {
    router.navigate(['/admin']);
    return false;
  }

  // Loan Officer goes to their dashboard
  if (currentUser?.role === 'LOAN_OFFICER') {
    router.navigate(['/officer']);
    return false;
  }

  // Not authenticated
  router.navigate(['/auth/login']);
  return false;
};
