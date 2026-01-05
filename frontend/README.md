# Loan Management System - Angular Frontend

## Architecture

### Modular Structure
- **Core Module**: Authentication, Interceptors, Guards, Services
- **Shared Module**: Reusable Components, Directives, Pipes
- **Feature Modules**: Loans, EMIs, Reports, Admin

## Features
- JWT Authentication with HTTP Interceptor
- Role-Based Access Control (RBAC)
- Reactive Forms with Validation
- Responsive UI with Angular Material
- Real-time Loan Status Tracking
- EMI Calculator & Schedule
- Admin Dashboard
- Reports & Analytics

## Setup Instructions

### Prerequisites
- Node.js 18+
- Angular CLI 17+

### Installation
```bash
npm install
```

### Development Server
```bash
ng serve
```
Navigate to `http://localhost:4200`

### Build
```bash
ng build --configuration production
```

## Project Structure
```
src/
├── app/
│   ├── core/                    # Core Module
│   │   ├── guards/             # Auth & Role Guards
│   │   ├── interceptors/       # JWT Interceptor
│   │   ├── services/           # Auth, API Services
│   │   └── models/             # Interfaces & Models
│   ├── shared/                  # Shared Module
│   │   ├── components/         # Reusable Components
│   │   ├── directives/         # Custom Directives
│   │   └── pipes/              # Custom Pipes
│   ├── features/                # Feature Modules
│   │   ├── auth/               # Login, Register
│   │   ├── loans/              # Loan Management
│   │   ├── emi/                # EMI Schedule
│   │   ├── reports/            # Reports & Analytics
│   │   ├── admin/              # Admin Panel
│   │   └── dashboard/          # User Dashboard
│   └── app.component.ts        # Root Component
└── environments/                # Environment Configs
```

## Pages
- **Login/Register**: User authentication
- **Dashboard**: Overview of loans, EMIs, notifications
- **Apply for Loan**: Loan application form
- **Loan Status**: Track loan application status
- **EMI Schedule**: View EMI payment schedule
- **Reports**: Loan reports and analytics
- **Admin Panel**: User management, loan approvals

## API Integration
Backend API Gateway: `http://localhost:8080`

### Endpoints
- POST `/api/auth/login` - User login
- POST `/api/auth/register` - User registration
- POST `/api/loan-applications/apply` - Apply for loan
- GET `/api/loan-applications/user/{userId}` - Get user applications
- GET `/api/loans/user/{userId}` - Get user loans
- GET `/api/loans/{loanId}/emi-schedule` - Get EMI schedule
- GET `/api/reports/*` - Reports endpoints

## Authentication Flow
1. User logs in → JWT token received
2. Token stored in localStorage
3. Interceptor adds token to all API requests
4. Guards protect routes based on authentication & roles
5. Token refresh on expiry

## Roles
- **USER**: Apply for loans, view own loans, track EMI
- **ADMIN**: Approve/reject loans, view all applications, manage users
