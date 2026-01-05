import { Injectable } from '@angular/core';
import { HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { environment } from '@environments/environment';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = localStorage.getItem(environment.tokenKey);

    if (token && !req.url.includes('/auth/login') && !req.url.includes('/auth/register')) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req);
  }
}
