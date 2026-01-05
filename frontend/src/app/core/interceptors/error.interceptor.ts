import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {

        // 🚨 VERY IMPORTANT
        // status === 0 means browser blocked response (CORS / network)
        // BACKEND MAY HAVE SUCCEEDED (201 Created)
        if (error.status === 0) {
          console.warn('Browser-level block (CORS or network). Ignoring.');
          return throwError(() => error);
        }

        let message = 'Unexpected error';

        switch (error.status) {
          case 400:
            message = error.error?.message || 'Bad request';
            break;
          case 401:
            message = 'Unauthorized. Please login again.';
            break;
          case 403:
            message = 'Forbidden';
            break;
          case 404:
            message = 'Not found';
            break;
          case 500:
            message = 'Server error. Try again later.';
            break;
        }

        console.error('HTTP Error:', error.status, message);

        return throwError(() => ({
          status: error.status,
          message
        }));
      })
    );
  }
}
