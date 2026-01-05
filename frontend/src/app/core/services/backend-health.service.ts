import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { environment } from '@environments/environment';

interface BackendStatus {
  status: 'healthy' | 'unhealthy';
  message: string;
  timestamp: Date;
  baseUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class BackendHealthService {
  
  constructor(private http: HttpClient) { }

  /**
   * Check if backend is reachable
   */
  checkBackendHealth(): Observable<BackendStatus> {
    // Try to call a public endpoint that doesn't require authentication
    const healthCheck$ = this.http.get<any>(`${environment.apiUrl}/auth/health`, {
      withCredentials: false
    }).pipe(
      timeout(5000), // 5 second timeout
      catchError(() => {
        // If health endpoint doesn't exist, try another simple endpoint
        return this.http.get<any>(`${environment.apiUrl}/auth/login`, {
          withCredentials: false
        }).pipe(
          timeout(5000),
          catchError(() => {
            return of(null);
          })
        );
      })
    );

    return new Observable(observer => {
      healthCheck$.subscribe({
        next: () => {
          observer.next({
            status: 'healthy',
            message: 'Backend is reachable',
            timestamp: new Date(),
            baseUrl: environment.apiUrl
          });
          observer.complete();
        },
        error: (error) => {
          const message = error.name === 'TimeoutError' 
            ? 'Backend timeout - service is slow or unreachable'
            : 'Backend is unreachable - check if it\'s running and CORS is configured';
          
          observer.next({
            status: 'unhealthy',
            message,
            timestamp: new Date(),
            baseUrl: environment.apiUrl
          });
          observer.complete();
        }
      });
    });
  }

  /**
   * Get configured API URL for debugging
   */
  getConfiguredUrl(): string {
    return environment.apiUrl;
  }
}
