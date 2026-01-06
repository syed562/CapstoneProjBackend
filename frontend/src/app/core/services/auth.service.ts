import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '@environments/environment';
import { User, LoginRequest, RegisterRequest, ChangePasswordRequest, ChangePasswordResponse } from '../models/models';
import { ErrorHandlerService } from './error-handler.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(
    private http: HttpClient,
    private router: Router,
    private errorHandler: ErrorHandlerService
  ) {
    const storedUser = localStorage.getItem(environment.userKey);
    this.currentUserSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  // 🎯 GET ROLE-BASED REDIRECT PATH
  public getRedirectPath(): string {
    const user = this.currentUserValue;
    
    if (!user) {
      return '/auth/login';
    }

    // Admin and Loan Officer skip profile completion
    if (user.role === 'ADMIN') {
      return '/admin';
    }
    
    if (user.role === 'LOAN_OFFICER') {
      return '/officer';
    }

    // Customer goes to profile completion first
    return '/complete-profile';
  }

  // 🔐 LOGIN (JWT FROM RESPONSE BODY)
  login(credentials: LoginRequest): Observable<any> {
    return this.http.post<any>(
      `${environment.apiUrl}/auth/login`,
      credentials
    ).pipe(
      tap((body) => {
        const token = body.token;

        if (!token) {
          throw new Error('Authorization token not found in response');
        }

        // Remove 'Bearer ' prefix if present
        const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;

        // Store JWT
        localStorage.setItem(environment.tokenKey, cleanToken);

        // Build user object
        const normalizedRole = (body.role ?? '').toString().trim().toUpperCase();

        const user: User = {
          userId: body.userId ?? null,
          username: body.username ?? credentials.username,
          email: body.email ?? '',
          role: (normalizedRole || 'CUSTOMER') as 'CUSTOMER' | 'ADMIN' | 'LOAN_OFFICER'
        };

        // Store user
        localStorage.setItem(environment.userKey, JSON.stringify(user));
        this.currentUserSubject.next(user);

        this.errorHandler.showSuccess('Login successful!');
      }),
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  // 📝 REGISTER
  register(data: RegisterRequest): Observable<any> {
    return this.http.post(
      `${environment.apiUrl}/auth/register`,
      data,
      { withCredentials: false }
    ).pipe(
      tap(() => {
        this.errorHandler.showSuccess('Registration successful! Please login.');
      }),
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  // 🔒 CHANGE PASSWORD
  changePassword(payload: ChangePasswordRequest): Observable<ChangePasswordResponse> {
    return this.http.post<ChangePasswordResponse>(
      `${environment.apiUrl}/auth/change-password`,
      payload
    ).pipe(
      tap((res) => {
        const message = res?.message || 'Password updated successfully';
        this.errorHandler.showSuccess(message);
      }),
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  // 🚪 LOGOUT
  logout(): void {
    localStorage.removeItem(environment.tokenKey);
    localStorage.removeItem(environment.userKey);
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  // 🔑 TOKEN
  getToken(): string | null {
    return localStorage.getItem(environment.tokenKey);
  }

  // 🔐 AUTH CHECKS
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasRole(role: 'CUSTOMER' | 'ADMIN' | 'LOAN_OFFICER'): boolean {
    return this.currentUserValue?.role === role;
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  isCustomer(): boolean {
    return this.hasRole('CUSTOMER');
  }

  isLoanOfficer(): boolean {
    return this.hasRole('LOAN_OFFICER');
  }
}
