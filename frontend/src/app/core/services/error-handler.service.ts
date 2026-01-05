import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface AppError {
  message: string;
  status?: number;
  details?: any;
  timestamp?: Date;
}

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {
  private errorSubject = new BehaviorSubject<AppError | null>(null);
  public error$ = this.errorSubject.asObservable();

  private successSubject = new BehaviorSubject<string | null>(null);
  public success$ = this.successSubject.asObservable();

  constructor() { }

  /**
   * Handle and broadcast error
   */
  handleError(error: any): void {
    const appError: AppError = {
      message: this.getErrorMessage(error),
      status: error?.status,
      details: error?.originalError || error,
      timestamp: new Date()
    };

    console.error('[ErrorHandlerService] Error:', appError);
    this.errorSubject.next(appError);
  }

  /**
   * Clear current error
   */
  clearError(): void {
    this.errorSubject.next(null);
  }

  /**
   * Broadcast success message
   */
  showSuccess(message: string): void {
    this.successSubject.next(message);
    // Auto-clear after 5 seconds
    setTimeout(() => this.successSubject.next(null), 5000);
  }

  /**
   * Clear success message
   */
  clearSuccess(): void {
    this.successSubject.next(null);
  }

  /**
   * Get human-readable error message
   */
  private getErrorMessage(error: any): string {
    if (typeof error === 'string') {
      return error;
    }

    if (error?.message) {
      return error.message;
    }

    if (error?.error?.message) {
      return error.error.message;
    }

    if (error?.status === 0) {
      return 'Network error or CORS issue. Make sure the backend is running at the configured URL.';
    }

    if (error?.status === 400) {
      return error?.error?.message || 'Invalid request. Please check your input.';
    }

    if (error?.status === 401) {
      return 'Your session has expired. Please login again.';
    }

    if (error?.status === 403) {
      return 'You do not have permission to perform this action.';
    }

    if (error?.status === 404) {
      return 'The requested resource was not found.';
    }

    if (error?.status === 500) {
      return 'Server error. Please try again later or contact support.';
    }

    if (error?.status === 503) {
      return 'The service is temporarily unavailable. Please try again later.';
    }

    if (error?.status) {
      return `Server error (${error.status}): ${error.statusText || 'Unknown error'}`;
    }

    return 'An unexpected error occurred. Please try again.';
  }

  /**
   * Get current error
   */
  getCurrentError(): AppError | null {
    return this.errorSubject.value;
  }

  /**
   * Check if there's an active error
   */
  hasError(): boolean {
    return this.errorSubject.value !== null;
  }
}
