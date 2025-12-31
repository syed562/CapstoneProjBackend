# Testing Loan Approval Rules - Complete API Examples

## Complete End-to-End Workflow

### Step 1: Register Customer

```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "email": "customer1@example.com",
    "password": "Password@123",
    "role": "CUSTOMER"
  }'
```

**Response:**
```json
{
  "id": "user-123",
  "username": "customer1",
  "email": "customer1@example.com",
  "role": "CUSTOMER"
}
```

---

### Step 2: Login to Get JWT Token

```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "password": "Password@123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "user-123",
  "role": "CUSTOMER"
}
```

Save the token as `CUSTOMER_TOKEN`

---

### Step 3: Update Customer Profile with Credit Info

⚠️ **IMPORTANT:** This is crucial for approval criteria!

```bash
curl -X PUT http://localhost:8086/api/profiles/user-123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer CUSTOMER_TOKEN" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "customer1@example.com",
    "phone": "9876543210",
    "creditScore": 750,
    "annualIncome": 2500000,
    "totalLiabilities": 100000,
    "kycStatus": "APPROVED"
  }'
```

**Response:**
```json
{
  "userId": "user-123",
  "firstName": "John",
  "lastName": "Doe",
  "creditScore": 750,
  "annualIncome": 2500000,
  "totalLiabilities": 100000,
  "kycStatus": "APPROVED"
}
```

---

### Step 4: Apply for Loan

```bash
curl -X POST http://localhost:8084/api/loan-applications/apply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer CUSTOMER_TOKEN" \
  -d '{
    "userId": "user-123",
    "loanType": "PERSONAL",
    "amount": 500000,
    "termMonths": 60,
    "ratePercent": 9.5
  }'
```

**Response:**
```json
{
  "id": "app-456",
  "userId": "user-123",
  "loanType": "PERSONAL",
  "amount": 500000,
  "termMonths": 60,
  "ratePercent": 9.5,
  "status": "SUBMITTED",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

Save application ID as `APP_ID`

---

### Step 5: Register Loan Officer

```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "officer1",
    "email": "officer1@example.com",
    "password": "Password@123",
    "role": "LOAN_OFFICER"
  }'
```

**Response:**
```json
{
  "id": "officer-789",
  "username": "officer1",
  "email": "officer1@example.com",
  "role": "LOAN_OFFICER"
}
```

---

### Step 6: Loan Officer Logs In

```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "officer1",
    "password": "Password@123"
  }'
```

Save token as `OFFICER_TOKEN`

---

### Step 7: Mark Application for Review

```bash
curl -X PUT http://localhost:8084/api/loan-applications/app-456/review \
  -H "Authorization: Bearer OFFICER_TOKEN"
```

**Response:**
```json
{
  "id": "app-456",
  "status": "UNDER_REVIEW",
  "updatedAt": "2025-01-15T10:35:00Z"
}
```

---

### Step 8: Approve Application (With Criteria Validation)

```bash
curl -X PUT http://localhost:8084/api/loan-applications/app-456/approve \
  -H "Authorization: Bearer OFFICER_TOKEN"
```

**Success Response (Approval):**
```json
{
  "id": "app-456",
  "userId": "user-123",
  "status": "APPROVED",
  "remarks": "Approved based on credit score, income, and liability checks",
  "updatedAt": "2025-01-15T10:40:00Z"
}
```

**Console Output (Notifications):**
```
[NOTIFICATION] Loan Approval - User: user-123, Application: app-456, Amount: 500000
[NOTIFICATION MESSAGE]
Loan Application Approved!
Application ID: app-456
Loan Type: PERSONAL
Amount: 500000.00
Tenure: 60 months
Interest Rate: 9.50%
Your EMI schedule has been generated. Please login to view details.

[LOAN NOTIFICATION] EMI Schedule - User: user-123, Loan: loan-789, EMI Amount: 9749.25
[LOAN NOTIFICATION MESSAGE]
EMI Schedule Generated Successfully!
Loan ID: loan-789
Loan Amount: 500000.00
Interest Rate: 9.50%
Tenure: 60 months
Monthly EMI: 9749.25
Total Interest: 84955.00
Total Payable: 584955.00
Your first payment is due next month. Please ensure timely payments to maintain good credit standing.
```

---

## Failure Scenarios

### Scenario 1: Low Credit Score

**Setup:**
```bash
# Update profile with low credit score
curl -X PUT http://localhost:8086/api/profiles/user-123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer CUSTOMER_TOKEN" \
  -d '{
    "creditScore": 550,
    "annualIncome": 2500000,
    "totalLiabilities": 100000
  }'
```

**Try to Approve:**
```bash
curl -X PUT http://localhost:8084/api/loan-applications/app-456/approve \
  -H "Authorization: Bearer OFFICER_TOKEN"
```

**Error Response:**
```json
{
  "status": 400,
  "message": "Loan cannot be approved: Credit score 550 is below minimum required score of 600",
  "path": "/api/loan-applications/app-456/approve",
  "timestamp": "2025-01-15T10:45:00Z"
}
```

**Console Output (Rejection Notification):**
```
[NOTIFICATION] Loan Rejection - User: user-123, Application: app-456
[NOTIFICATION MESSAGE]
Loan Application Rejected
Application ID: app-456
Reason: Credit score 550 is below minimum required score of 600
Please contact our support team for more details or to reapply.
```

---

### Scenario 2: Insufficient Income

**Setup:**
```bash
# Update profile with insufficient income
curl -X PUT http://localhost:8086/api/profiles/user-123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer CUSTOMER_TOKEN" \
  -d '{
    "creditScore": 750,
    "annualIncome": 50000,
    "totalLiabilities": 100000
  }'
```

**Try to Approve:**
```bash
curl -X PUT http://localhost:8084/api/loan-applications/app-456/approve \
  -H "Authorization: Bearer OFFICER_TOKEN"
```

**Error Response:**
```json
{
  "status": 400,
  "message": "Loan cannot be approved: Annual income (50000) is insufficient. Required: 100000 for loan amount 500000",
  "path": "/api/loan-applications/app-456/approve",
  "timestamp": "2025-01-15T10:50:00Z"
}
```

---

### Scenario 3: Excessive Liabilities

**Setup:**
```bash
# Update profile with excessive liabilities
curl -X PUT http://localhost:8086/api/profiles/user-123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer CUSTOMER_TOKEN" \
  -d '{
    "creditScore": 750,
    "annualIncome": 2500000,
    "totalLiabilities": 300000
  }'
```

**Try to Approve:**
```bash
curl -X PUT http://localhost:8084/api/loan-applications/app-456/approve \
  -H "Authorization: Bearer OFFICER_TOKEN"
```

**Error Response:**
```json
{
  "status": 400,
  "message": "Loan cannot be approved: Total liabilities (300000) exceed allowed limit (250000) for loan amount 500000",
  "path": "/api/loan-applications/app-456/approve",
  "timestamp": "2025-01-15T10:55:00Z"
}
```

---

## Testing All Criteria Combinations

### Test Case Matrix

| Credit Score | Annual Income | Liabilities | Expected |
|---|---|---|---|
| 750 | 2500000 | 100000 | ✅ APPROVE |
| 550 | 2500000 | 100000 | ❌ REJECT (credit) |
| 750 | 50000 | 100000 | ❌ REJECT (income) |
| 750 | 2500000 | 300000 | ❌ REJECT (liability) |
| 600 | 100000 | 250000 | ✅ APPROVE (minimum thresholds) |
| 599 | 2500000 | 100000 | ❌ REJECT (just below credit min) |

---

## View Approved Loan

```bash
# Get the created loan
curl -X GET http://localhost:8085/api/loans/loan-789 \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

**Response:**
```json
{
  "id": "loan-789",
  "userId": "user-123",
  "amount": 500000,
  "termMonths": 60,
  "loanType": "PERSONAL",
  "ratePercent": 9.5,
  "status": "approved",
  "createdAt": "2025-01-15T10:40:00Z"
}
```

---

## View EMI Schedule

```bash
# Get EMI schedule (60 months)
curl -X GET http://localhost:8085/api/loans/loan-789/emi \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

**Response (First 3 months shown):**
```json
[
  {
    "id": "emi-1",
    "loanId": "loan-789",
    "month": 1,
    "emiAmount": 9749.25,
    "principalAmount": 7549.25,
    "interestAmount": 2200.00,
    "outstandingBalance": 492450.75,
    "status": "SCHEDULED",
    "dueDate": "2025-02-28"
  },
  {
    "id": "emi-2",
    "loanId": "loan-789",
    "month": 2,
    "emiAmount": 9749.25,
    "principalAmount": 7591.67,
    "interestAmount": 2157.58,
    "outstandingBalance": 484859.08,
    "status": "SCHEDULED",
    "dueDate": "2025-03-31"
  },
  ...
]
```

---

## Postman Collection Import

You can also import these as a Postman collection:

1. Create environment variables:
   - `BASE_URL`: http://localhost:8083
   - `CUSTOMER_TOKEN`: [token from login]
   - `OFFICER_TOKEN`: [token from officer login]
   - `APP_ID`: [application ID]
   - `USER_ID`: [customer user ID]

2. Create requests for each step above

3. Test different profile scenarios to see approval/rejection

---

## Notes

- Loan Officer can **ONLY** approve if ALL three criteria are met:
  - Credit score ≥ 600
  - Annual income ≥ loan amount / 5
  - Total liabilities ≤ loan amount × 0.5

- Each failed criterion provides specific rejection reason

- Notifications are logged to console (can integrate email/SMS later)

- EMI is automatically calculated and 60-month schedule generated

- Customer receives both approval AND EMI notifications

---

## Troubleshooting

**Issue:** "Loan cannot be approved: Customer profile not found"
- **Solution:** Update customer profile first with creditScore, annualIncome, totalLiabilities

**Issue:** Getting 403 Forbidden on approval endpoint
- **Solution:** Use LOAN_OFFICER token, not CUSTOMER token

**Issue:** Notifications not showing
- **Solution:** Check console/logs - they are logged as INFO level

**Issue:** EMI schedule shows 0 months
- **Solution:** Ensure termMonths is between 1-60
