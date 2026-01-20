import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, catchError, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private API = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  login() {
    window.location.href =
      'http://localhost:8080/oauth2/authorization/google';
  }

logout() {
  return this.http.post(
    `${this.API}/auth/logout`,
    {},
    { withCredentials: true }
  ).subscribe({
    next: () => {
      window.location.href = '/login';
    },
    error: err => {
      console.error('Logout failed', err);
      window.location.href = '/login'; // force exit anyway
    }
  });
}



  getCurrentUser() {
  return this.http.get<any>(
    `${this.API}/api/me`,
    { withCredentials: true }
  );
}


  /** 🔐 SESSION CHECK */
  isAuthenticated() {
    return this.http.get(`${this.API}/customers`, {
      withCredentials: true
    }).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }
}

