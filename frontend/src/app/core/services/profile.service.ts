import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Profile } from '../models/models';
import { ErrorHandlerService } from './error-handler.service';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = `${environment.apiUrl}/profiles`;

  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlerService
  ) {}

  getProfile(userId: string): Observable<Profile> {
    return this.http.get<Profile>(`${this.apiUrl}/${userId}`).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  createProfile(profile: Partial<Profile>, userId: string): Observable<Profile> {
    // Backend supports upsert via PUT /api/profiles/me?userId=...
    return this.http.put<Profile>(`${this.apiUrl}/me`, profile, {
      params: { userId }
    }).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  updateProfile(userId: string, profile: Partial<Profile>): Observable<Profile> {
    return this.http.put<Profile>(`${this.apiUrl}/${userId}`, profile).pipe(
      catchError(error => {
        this.errorHandler.handleError(error);
        throw error;
      })
    );
  }

  checkProfileExists(userId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${userId}/exists`).pipe(
      catchError(() => {
        // If profile doesn't exist, return false instead of throwing error
        return [false];
      })
    );
  }
}
