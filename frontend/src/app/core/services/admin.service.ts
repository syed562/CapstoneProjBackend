import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { ErrorHandlerService } from './error-handler.service';
import { catchError, tap } from 'rxjs/operators';

export interface LoanRate {
  [key: string]: number;
}

export interface UpdateRateRequest {
  loanType: string;
  rate: number;
}

export interface UpdateRateResponse {
  message: string;
  loanType: string;
  rate: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlerService
  ) {}

  /**
   * Get all current loan type rates
   */
  getAllRates(): Observable<LoanRate> {
    return this.http.get<LoanRate>(`${environment.apiUrl}/rates`).pipe(
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  /**
   * Update rate for a specific loan type (Admin only)
   */
  updateRate(request: UpdateRateRequest): Observable<UpdateRateResponse> {
    return this.http.post<UpdateRateResponse>(
      `${environment.apiUrl}/rates/update`,
      request
    ).pipe(
      tap((res) => {
        this.errorHandler.showSuccess(`${request.loanType} rate updated to ${request.rate}%`);
      }),
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  /**
   * Reset all rates to defaults (Admin only)
   */
  resetRatesToDefaults(): Observable<any> {
    return this.http.post<any>(
      `${environment.apiUrl}/rates/reset`,
      {}
    ).pipe(
      tap((res) => {
        this.errorHandler.showSuccess('All rates reset to defaults');
      }),
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }
}
