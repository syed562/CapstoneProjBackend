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

  getOverallReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/overall`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getUserReport(userId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/user/${userId}`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getLoanApplicationsReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/applications`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getLoansReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/loans`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  getRevenueReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/revenue`, {
      withCredentials: false
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }
}
