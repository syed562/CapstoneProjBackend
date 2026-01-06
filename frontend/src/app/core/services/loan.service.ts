import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '@environments/environment';
import { LoanApplication } from '../models/models';
import { ErrorHandlerService } from './error-handler.service';

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private apiUrl = `${environment.apiUrl}/loan-applications`;

  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlerService
  ) {}

  applyForLoan(application: Partial<LoanApplication>): Observable<LoanApplication> {
    return this.http.post<LoanApplication>(`${this.apiUrl}/apply`, application, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getUserApplications(userId: string): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.apiUrl}/user/${userId}`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getAllApplications(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(this.apiUrl, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getApplicationById(id: string): Observable<LoanApplication> {
    return this.http.get<LoanApplication>(`${this.apiUrl}/${id}`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  approveApplication(id: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/approve`, {}, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  markUnderReview(id: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/review`, {}, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  rejectApplication(id: string, reason: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/reject`, { remarks: reason }, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getUserLoans(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/loans/user/${userId}`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getAllLoans(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/loans`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getLoanById(loanId: string): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/loans/${loanId}`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getEMISchedule(loanId: string): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/loans/${loanId}/emi-schedule`).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getPaymentsByLoan(loanId: string): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/payments/loan/${loanId}`).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  makeEMIPayment(loanId: string, emiNumber: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/loans/${loanId}/pay-emi`, { emiNumber }, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }
}
