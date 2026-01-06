import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '@environments/environment';
import { ErrorHandlerService } from './error-handler.service';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlerService
  ) {}

  getLoanStatus(): Observable<any> {
    return this.http.get(`${this.apiUrl}/loan-status`).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getCustomerSummary(): Observable<any> {
    return this.http.get(`${this.apiUrl}/customer-summary`).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getDashboard(): Observable<any> {
    return this.http.get(`${this.apiUrl}/dashboard`).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getComprehensive(): Observable<any> {
    return this.http.get(`${this.apiUrl}/comprehensive`).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }
}
