import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { User } from './core/models/models';
import { MatSidenav } from '@angular/material/sidenav';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'Loan Management System';
  currentUser: User | null = null;
  isAuthenticated = false;
  @ViewChild('sidenav') sidenav!: MatSidenav;

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      this.isAuthenticated = !!user;
    });
  }

  logout() {
    this.authService.logout();
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
  }
}
