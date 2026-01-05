import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const loanOfficerGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  const currentUser = authService.currentUserValue;
  
  if (currentUser && (currentUser.role === 'LOAN_OFFICER' || currentUser.role === 'ADMIN')) {
    return true;
  }
  
  router.navigate(['/dashboard']);
  return false;
};
