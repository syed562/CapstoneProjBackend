# Loan Approval Rules Implementation

This document describes the implementation of the Loan Approval Rules in the Capstone Loan Management System.

## Rules Implemented

### 1. Who Can Approve Loans ✅
**Rule:** Only users with the Loan Officer role can approve or reject loan applications. Customers cannot approve their own loans.

**Implementation:**
- All approval/rejection endpoints use `@PreAuthorize("hasRole('LOAN_OFFICER') or hasRole('ADMIN')")`
- Enforced at the Spring Security level in `LoanApplicationController`
- Endpoints: `PUT /api/loan-applications/{id}/approve`, `PUT /api/loan-applications/{id}/reject`

---

### 2. Criteria for Approval ✅
**Rule:** A loan is approved based on:
- Customer's credit score
- Income eligibility
- Existing financial liabilities

**Implementation:**

#### 2a. Profile Enhancement
**File:** `profile-service/src/main/java/com/example/profileservice/domain/Profile.java`

Added three new fields to Profile entity:
```java
private Double creditScore;        // Credit score (0-900)
private Double annualIncome;       // Annual income for eligibility
private Double totalLiabilities;   // Total outstanding liabilities
```

#### 2b. Approval Criteria Engine
**File:** `loan-application-service/src/main/java/com/example/loanapplication/service/ApprovalCriteriaService.java`

New service that validates three criteria:

1. **Credit Score Check**
   - Minimum credit score threshold: 600 (configurable)
   - Rejects if customer's score is below minimum

2. **Income Eligibility Check**
   - Formula: `Annual Income ≥ Loan Amount / Income Multiplier`
   - Default multiplier: 5 (i.e., customer should earn at least 20% of loan amount annually)
   - Rejects if income is insufficient

3. **Financial Liability Check**
   - Formula: `Total Liabilities ≤ Loan Amount × Liability Multiplier`
   - Default multiplier: 0.5 (i.e., liabilities shouldn't exceed 50% of loan amount)
   - Rejects if customer has too much existing debt

**Configuration:**
File: `loan-application-service/src/main/resources/application.properties`
```properties
loan.approval.min.credit.score=600
loan.approval.income.multiplier=5
loan.approval.liability.multiplier=0.5
```

**Usage:**
```java
ApprovalCriteriaService.ApprovalDecision decision = 
    approvalCriteriaService.validateApprovalCriteria(userId, loanAmount);

if (!decision.isApproved()) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
        "Loan cannot be approved: " + decision.getReason());
}
```

---

### 3. Rejection Rules ✅
**Rule:** If a loan is rejected, the system must store a reason.

**Implementation:**
- Rejection reason stored in `LoanApplication.remarks` field
- `NotificationService.sendRejectionNotification()` logs rejection
- Rejection requires `@Valid @RequestBody ApprovalRequest` with mandatory `remarks` field

**Example Rejection Reasons:**
- "Credit score 550 is below minimum required score of 600"
- "Annual income (15000) is insufficient. Required: 100000 for loan amount 500000"
- "Total liabilities (300000) exceed allowed limit (250000) for loan amount 500000"

---

### 4. Actions After Approval ✅
**Rule:** Once a loan is approved:
- An EMI (monthly installment) schedule is automatically created ✅
- The customer receives a notification about approval and EMI details ✅

**Implementation:**

#### 4a. Automatic EMI Generation
**File:** `loan-service/src/main/java/com/example/loanservice/service/LoanService.java`

In `approveFromApplication()`:
```java
// Auto-generate EMI schedule after approval
emiService.generateEMISchedule(saved.getId());
```

Generates 60-month amortization schedule with:
- Monthly EMI amount (fixed)
- Principal breakdown
- Interest breakdown
- Outstanding balance for each month

#### 4b. Approval Notification
**File:** `loan-application-service/src/main/java/com/example/loanapplication/service/NotificationService.java`

Sends approval notification with:
- Application ID
- Loan type
- Amount
- Tenure
- Interest rate
- Message confirming EMI schedule generation

**Example Notification:**
```
[NOTIFICATION] Loan Approval - User: user-123, Application: app-456, Amount: 500000
Loan Application Approved!
Application ID: app-456
Loan Type: PERSONAL
Amount: 500000.00
Tenure: 60 months
Interest Rate: 9.50%
Your EMI schedule has been generated. Please login to view details.
```

#### 4c. EMI Notification
**File:** `loan-service/src/main/java/com/example/loanservice/service/LoanNotificationService.java`

Sends EMI details notification with:
- Loan ID
- Loan amount
- Interest rate
- Tenure
- Monthly EMI amount
- Total interest
- Total payable amount

**Example EMI Notification:**
```
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

## Workflow Diagram

```
Customer applies for loan
    ↓
Loan Application created (SUBMITTED status)
    ↓
Loan Officer reviews application
    ↓
Loan Officer clicks "Approve"
    ↓
System validates approval criteria:
  ├─ Credit score ≥ 600?
  ├─ Annual income ≥ loan amount/5?
  └─ Total liabilities ≤ loan amount×0.5?
    ↓
If ANY criteria fails → Rejection
  ├─ Rejection reason stored in remarks
  └─ Rejection notification sent
    ↓
If ALL criteria pass → Approval
  ├─ Application status = APPROVED
  ├─ Approval reason stored in remarks
  ├─ Approval notification sent
  ├─ Loan created
  ├─ EMI schedule auto-generated (60 months)
  └─ EMI notification sent
    ↓
Customer receives notifications
  ├─ Approval notification
  └─ EMI schedule notification
```

---

## Integration Points

### Service-to-Service Communication

1. **Loan Application Service → Profile Service**
   - Uses `ProfileServiceClient` (Feign) to fetch customer profile
   - Retrieves: creditScore, annualIncome, totalLiabilities

2. **Loan Application Service → Loan Service**
   - After approval, loan is created from application
   - Endpoint: `POST /api/loans/{applicationId}/approve`

3. **Loan Service → Notification Service**
   - Sends EMI notification after schedule generation
   - Includes calculated EMI amount and payment schedule details

---

## Testing the Implementation

### Test Case 1: Approve with Valid Criteria
```bash
# Step 1: Update customer profile with good credit score
PUT /api/profiles/{userId}
{
  "creditScore": 750,
  "annualIncome": 2500000,
  "totalLiabilities": 100000
}

# Step 2: Apply for loan
POST /api/loan-applications/apply
{
  "userId": "user-123",
  "loanType": "PERSONAL",
  "amount": 500000,
  "termMonths": 60,
  "ratePercent": 9.5
}

# Step 3: Mark for review
PUT /api/loan-applications/{appId}/review

# Step 4: Approve (should succeed)
PUT /api/loan-applications/{appId}/approve
```

**Expected Result:** ✅ Approval succeeds, notifications sent

### Test Case 2: Reject - Low Credit Score
```bash
# Update profile with low credit score
PUT /api/profiles/{userId}
{
  "creditScore": 550,
  "annualIncome": 2500000,
  "totalLiabilities": 100000
}

# Try to approve (should fail)
PUT /api/loan-applications/{appId}/approve
```

**Expected Result:** ❌ Rejected with message: "Credit score 550 is below minimum required score of 600"

### Test Case 3: Reject - Insufficient Income
```bash
# Update profile with insufficient income
PUT /api/profiles/{userId}
{
  "creditScore": 700,
  "annualIncome": 50000,  // Too low for 500000 loan
  "totalLiabilities": 100000
}

# Try to approve (should fail)
PUT /api/loan-applications/{appId}/approve
```

**Expected Result:** ❌ Rejected with message: "Annual income (50000) is insufficient. Required: 100000 for loan amount 500000"

---

## Configuration Customization

You can adjust approval criteria in `application.properties`:

```properties
# Minimum credit score (0-900)
loan.approval.min.credit.score=600

# Income requirement multiplier
# Annual income must be >= loan amount / multiplier
loan.approval.income.multiplier=5

# Liability limit multiplier
# Total liabilities must be <= loan amount * multiplier
loan.approval.liability.multiplier=0.5
```

**Examples:**
- Increase `income.multiplier` to 10 → stricter income requirement
- Decrease `liability.multiplier` to 0.3 → stricter liability requirement
- Increase `min.credit.score` to 700 → higher credit threshold

---

## Future Enhancements

1. **Email/SMS Notifications**
   - Replace logger-based notifications with actual email/SMS service
   - Add customer email and phone to notification messages

2. **Advanced Credit Scoring**
   - Machine learning model for credit score prediction
   - Automatic credit score updates based on repayment history

3. **Customizable Approval Rules**
   - Database-driven approval criteria
   - Per-loanType different thresholds
   - Time-based criteria adjustments

4. **Approval Audit Trail**
   - Store who approved/rejected and when
   - Reason tracking with timestamps
   - Loan officer performance analytics

5. **Collateral/Security Options**
   - Option to provide collateral to overcome low credit score
   - Collateral valuation integration

---

## Summary

All 4 Loan Approval Rules have been fully implemented:

✅ **Rule 1:** Only Loan Officers can approve loans  
✅ **Rule 2:** Approval based on credit score, income, and liabilities  
✅ **Rule 3:** Rejection reasons stored  
✅ **Rule 4:** Automatic EMI generation and notifications sent  

The system now provides a complete approval workflow with comprehensive validation and customer communication.
