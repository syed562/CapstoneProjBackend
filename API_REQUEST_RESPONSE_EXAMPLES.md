# API Request/Response Examples

This file shows correct request patterns and expected responses for the frontend to properly communicate with the backend.

## Authentication Endpoints

### 1. User Login

**Frontend Request:**
```typescript
// Using the updated AuthService
this.authService.login({
  username: 'john.doe',
  password: 'password123'
}).subscribe({
  next: (response) => console.log('Logged in!'),
  error: (error) => console.error('Login failed:', error)
});
```

**HTTP Details:**
```
Method: POST
URL: http://localhost:8080/api/auth/login
Headers:
  Content-Type: application/json
  Origin: http://localhost:4200
  
Body:
{
  "username": "john.doe",
  "password": "password123"
}

CORS Preflight (Options):
Method: OPTIONS
Access-Control-Request-Method: POST
```

**Success Response (200):**
```json
{
  "userId": "uuid-123",
  "username": "john.doe",
  "email": "john@example.com",
  "role": "CUSTOMER",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Response (401):**
```json
{
  "status": 401,
  "message": "Invalid username or password",
  "timestamp": "2025-01-05T10:30:00Z"
}
```

### 2. User Registration

**Frontend Request:**
```typescript
this.authService.register({
  username: 'jane.doe',
  password: 'password123',
  email: 'jane@example.com'
}).subscribe({
  next: () => console.log('Registration successful!'),
  error: (error) => console.error('Registration failed')
});
```

**HTTP Details:**
```
Method: POST
URL: http://localhost:8080/api/auth/register
```

**Success Response (201):**
```json
{
  "userId": "uuid-456",
  "username": "jane.doe",
  "email": "jane@example.com",
  "message": "User registered successfully"
}
```

**Error Response (400):**
```json
{
  "status": 400,
  "message": "Username already exists",
  "timestamp": "2025-01-05T10:30:00Z"
}
```

## Loan Application Endpoints

### 3. Get All Loan Applications

**Frontend Request:**
```typescript
this.loanService.getAllApplications().subscribe({
  next: (applications) => console.log('Got applications:', applications),
  error: (error) => console.error('Failed to fetch')
});
```

**HTTP Details:**
```
Method: GET
URL: http://localhost:8080/api/loan-applications
Headers:
  Authorization: Bearer <jwt_token>
  
CORS: Credentials included automatically
```

**Success Response (200):**
```json
[
  {
    "applicationId": "app-123",
    "userId": "user-456",
    "loanType": "PERSONAL",
    "requestedAmount": 100000.00,
    "status": "PENDING",
    "createdAt": "2025-01-05T10:00:00Z",
    "approvalRate": 0.12,
    "tenureMonths": 24
  },
  {
    "applicationId": "app-124",
    "userId": "user-456",
    "loanType": "HOME",
    "requestedAmount": 500000.00,
    "status": "APPROVED",
    "createdAt": "2025-01-04T15:00:00Z"
  }
]
```

**Error Response (401 - No Token):**
```json
{
  "status": 401,
  "message": "Unauthorized - Please login again",
  "timestamp": "2025-01-05T10:30:00Z"
}
```

### 4. Apply for Loan

**Frontend Request:**
```typescript
const application: Partial<LoanApplication> = {
  loanType: 'PERSONAL',
  requestedAmount: 100000,
  tenureMonths: 24
};

this.loanService.applyForLoan(application).subscribe({
  next: (newApp) => console.log('Application submitted:', newApp),
  error: (error) => console.error('Failed to apply')
});
```

**HTTP Details:**
```
Method: POST
URL: http://localhost:8080/api/loan-applications/apply
Headers:
  Authorization: Bearer <jwt_token>
  Content-Type: application/json
  
Body:
{
  "loanType": "PERSONAL",
  "requestedAmount": 100000.00,
  "tenureMonths": 24
}
```

**Success Response (201):**
```json
{
  "applicationId": "app-new-789",
  "userId": "user-456",
  "loanType": "PERSONAL",
  "requestedAmount": 100000.00,
  "status": "PENDING",
  "createdAt": "2025-01-05T10:45:00Z"
}
```

**Error Response (400 - Invalid Data):**
```json
{
  "status": 400,
  "message": "Requested amount exceeds maximum limit of 2000000",
  "timestamp": "2025-01-05T10:45:00Z"
}
```

### 5. Get User's Applications

**Frontend Request:**
```typescript
const userId = 'user-456';
this.loanService.getUserApplications(userId).subscribe({
  next: (apps) => console.log('Your applications:', apps)
});
```

**HTTP Details:**
```
Method: GET
URL: http://localhost:8080/api/loan-applications/user/{userId}
Headers:
  Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
[
  {
    "applicationId": "app-123",
    "userId": "user-456",
    "status": "PENDING",
    "createdAt": "2025-01-05T10:00:00Z"
  }
]
```

### 6. Approve Application (Loan Officer Only)

**Frontend Request:**
```typescript
this.loanService.approveApplication('app-123').subscribe({
  next: (result) => console.log('Approved!'),
  error: (error) => {
    if (error.status === 403) {
      console.error('You must be a Loan Officer');
    }
  }
});
```

**HTTP Details:**
```
Method: PUT
URL: http://localhost:8080/api/loan-applications/{applicationId}/approve
Headers:
  Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "applicationId": "app-123",
  "status": "APPROVED",
  "approvalDate": "2025-01-05T11:00:00Z"
}
```

**Error Response (403 - Insufficient Permission):**
```json
{
  "status": 403,
  "message": "You do not have permission to perform this action",
  "timestamp": "2025-01-05T11:00:00Z"
}
```

## Loan Endpoints

### 7. Get All Loans

**Frontend Request:**
```typescript
this.loanService.getAllApplications().subscribe({
  next: (loans) => console.log('Loans:', loans)
});
```

**HTTP Details:**
```
Method: GET
URL: http://localhost:8080/api/loans
Headers:
  Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
[
  {
    "loanId": "loan-001",
    "userId": "user-456",
    "amount": 100000.00,
    "loanType": "PERSONAL",
    "status": "ACTIVE",
    "tenureMonths": 24,
    "ratePercent": 12.0,
    "createdAt": "2025-01-05T10:00:00Z"
  }
]
```

### 8. Get EMI Schedule

**Frontend Request:**
```typescript
this.loanService.getEMISchedule('loan-001').subscribe({
  next: (schedule) => console.log('EMI Schedule:', schedule)
});
```

**HTTP Details:**
```
Method: GET
URL: http://localhost:8080/api/loans/{loanId}/emi-schedule
Headers:
  Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
[
  {
    "emiNumber": 1,
    "emiAmount": 4700.00,
    "principalAmount": 3800.00,
    "interestAmount": 900.00,
    "dueDate": "2025-02-05",
    "paidDate": null,
    "status": "PENDING"
  },
  {
    "emiNumber": 2,
    "emiAmount": 4700.00,
    "principalAmount": 3850.00,
    "interestAmount": 850.00,
    "dueDate": "2025-03-05",
    "paidDate": null,
    "status": "PENDING"
  }
]
```

## Profile Endpoints

### 9. Get User Profile

**Frontend Request:**
```typescript
this.http.get(`${environment.apiUrl}/profiles/{userId}`).subscribe({
  next: (profile) => console.log('Profile:', profile)
});
```

**HTTP Details:**
```
Method: GET
URL: http://localhost:8080/api/profiles/{userId}
Headers:
  Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "userId": "user-456",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "monthlyIncome": 50000.00,
  "employmentStatus": "EMPLOYED"
}
```

## Report Endpoints

### 10. Get Dashboard Report

**Frontend Request:**
```typescript
this.reportService.getOverallReport().subscribe({
  next: (report) => console.log('Dashboard:', report)
});
```

**HTTP Details:**
```
Method: GET
URL: http://localhost:8080/api/reports/overall
Headers:
  Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "totalLoans": 150,
  "totalApplications": 250,
  "totalDisbursed": 50000000.00,
  "totalRepaid": 10000000.00,
  "activeLoans": 120,
  "defaultedLoans": 5,
  "totalUsers": 1000,
  "totalRevenue": 2500000.00
}
```

## Error Response Patterns

### Pattern 1: Validation Error (400)
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "requestedAmount",
      "message": "Amount must be greater than 10000"
    },
    {
      "field": "tenureMonths",
      "message": "Tenure must be between 6 and 60 months"
    }
  ],
  "timestamp": "2025-01-05T10:45:00Z"
}
```

### Pattern 2: Not Found (404)
```json
{
  "status": 404,
  "message": "Loan application not found",
  "requestedId": "app-invalid-123",
  "timestamp": "2025-01-05T10:45:00Z"
}
```

### Pattern 3: Server Error (500)
```json
{
  "status": 500,
  "message": "An unexpected error occurred. Please try again later.",
  "timestamp": "2025-01-05T10:45:00Z",
  "traceId": "abc123def456"
}
```

### Pattern 4: CORS Error (0)
```
Network tab shows:
- No response from server
- No Access-Control-Allow-Origin header
- Status: 0 or (failed)

Browser console shows:
"Access-Control-Allow-Origin" header is missing
```

## Testing with Postman/cURL

### Test Login (cURL):
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:4200" \
  -d '{
    "username": "john.doe",
    "password": "password123"
  }'
```

### Test with Token (cURL):
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/loans \
  -H "Authorization: Bearer $TOKEN" \
  -H "Origin: http://localhost:4200"
```

### Test CORS Preflight (cURL):
```bash
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type" \
  -v
```

## Frontend Error Handling Examples

### In Component:
```typescript
export class LoanListComponent implements OnInit {
  loans$ = this.loanService.getAllApplications().pipe(
    catchError(error => {
      // Error is already handled by interceptor
      // This is just for additional component-level handling
      console.error('Component-level error handler:', error);
      return of([]); // Return empty array on error
    })
  );

  error$ = this.errorHandler.error$;
  success$ = this.errorHandler.success$;

  constructor(
    private loanService: LoanService,
    private errorHandler: ErrorHandlerService
  ) {}

  retryLoadLoans() {
    this.errorHandler.clearError();
    this.loans$ = this.loanService.getAllApplications();
  }
}
```

### In Template:
```html
<div *ngIf="error$ | async as error" class="alert alert-danger">
  <h5>Error</h5>
  <p>{{ error.message }}</p>
  <button (click)="retryLoadLoans()">Retry</button>
</div>

<div *ngIf="(loans$ | async) as loans">
  <div *ngIf="loans.length === 0" class="alert alert-info">
    No loans found
  </div>
  <table *ngIf="loans.length > 0">
    <tr *ngFor="let loan of loans">
      <td>{{ loan.loanId }}</td>
      <td>{{ loan.amount | currency }}</td>
      <td>{{ loan.status }}</td>
    </tr>
  </table>
</div>
```

---

**Last Updated:** 2025-01-05
