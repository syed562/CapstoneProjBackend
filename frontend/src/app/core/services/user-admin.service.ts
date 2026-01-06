import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { environment } from '@environments/environment';
import { ErrorHandlerService } from './error-handler.service';

export interface AdminUserView {
  id: string;
  username: string;
  email: string;
  role: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  private apiUrl = `${environment.apiUrl}/auth/users`;

  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlerService
  ) {}

  listUsers(): Observable<AdminUserView[]> {
    return this.http.get<AdminUserView[]>(this.apiUrl).pipe(
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  updateRole(userId: string, role: string): Observable<AdminUserView> {
    return this.http.put<AdminUserView>(`${this.apiUrl}/${userId}/role`, { role }).pipe(
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  updateStatus(userId: string, status: string): Observable<AdminUserView> {
    return this.http.put<AdminUserView>(`${this.apiUrl}/${userId}/status`, { status }).pipe(
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`).pipe(
      catchError((error) => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }
}
