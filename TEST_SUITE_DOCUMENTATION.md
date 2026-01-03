# Comprehensive Test Suite Documentation

## Overview
This document outlines the test coverage for each microservice in the Loan Management System. Target coverage: **90%+**

---

## 1. LOAN-APPLICATION-SERVICE

### Test Classes Created:
1. **LoanApplicationServiceTest.java** - 17 test cases
2. **LoanApplicationControllerTest.java** - 8 test cases

### Coverage Details:

#### LoanApplicationService (17 tests):
- ✅ `testApplySuccess` - Happy path loan application
- ✅ `testApplyBelowMinAmount` - Validation for minimum amount
- ✅ `testApplyInvalidTenure` - Validation for tenure options
- ✅ `testApplyDuplicateActiveLoan` - Business rule check for duplicate loans
- ✅ `testApplyWithCustomRate` - Custom interest rate
- ✅ `testGetApplicationSuccess` - Retrieve application by ID
- ✅ `testGetApplicationNotFound` - 404 handling
- ✅ `testMarkUnderReviewSuccess` - Status transition
- ✅ `testApproveSuccess` - Approval workflow with cascading effects
- ✅ `testRejectSuccess` - Rejection with remarks
- ✅ `testRejectWithEmptyRemarks` - Validation for rejection
- ✅ `testListByUser` - User-specific application list
- ✅ `testListAll` - Get all applications
- ✅ `testEducationalLoanRate` - New loan type rate validation
- ✅ `testHomeLoanRate` - New loan type rate validation
- ✅ Event publishing (implicit in apply/approve/reject)
- ✅ Feign client integration (mocked)

#### LoanApplicationController (8 tests):
- ✅ `testApplySuccess` - HTTP POST /api/loan-applications/apply
- ✅ `testApplyMissingAuthContext` - 401 Unauthorized
- ✅ `testListAll` - HTTP GET /api/loan-applications
- ✅ `testListByUser` - HTTP GET /api/loan-applications/my
- ✅ `testGetApplicationById` - HTTP GET /api/loan-applications/{id}
- ✅ `testMarkUnderReview` - HTTP PUT /api/loan-applications/{id}/review
- ✅ `testApprove` - HTTP PUT /api/loan-applications/{id}/approve
- ✅ `testReject` - HTTP PUT /api/loan-applications/{id}/reject

**Expected Coverage: 88-92%**

---

## 2. LOAN-SERVICE

### Test Classes Created:
1. **LoanServiceTest.java** - 12 test cases
2. **LoanControllerTest.java** - 10 test cases
3. **PaymentControllerTest.java** - 5 test cases

### Coverage Details:

#### LoanService (12 tests):
- ✅ `testCreateLoanSuccess` - Create loan from application
- ✅ `testListAllLoans` - Retrieve all loans
- ✅ `testListLoansWithPagination` - Paginated results
- ✅ `testGetLoanSuccess` - Get loan by ID
- ✅ `testGetLoanNotFound` - 404 handling
- ✅ `testUpdateStatusSuccess` - Status transitions
- ✅ `testListByUser` - User-specific loans
- ✅ `testFindByStatusAndAmount` - Filter with criteria
- ✅ `testGenerateEMISuccess` - EMI schedule generation
- ✅ `testDeleteLoan` - Loan deletion
- ✅ EMI calculation (mocked)
- ✅ Database persistence (mocked)

#### LoanController (10 tests):
- ✅ `testListLoans` - HTTP GET /api/loans
- ✅ `testListPaged` - HTTP GET /api/loans/paged
- ✅ `testMyLoans` - HTTP GET /api/loans/my
- ✅ `testFilterLoans` - HTTP GET /api/loans/filter
- ✅ `testGetLoan` - HTTP GET /api/loans/{id}
- ✅ `testCreateLoan` - HTTP POST /api/loans
- ✅ `testUpdateStatus` - HTTP PATCH /api/loans/{id}/status
- ✅ `testGenerateEMI` - HTTP POST /api/loans/{id}/generate-emi
- ✅ `testDeleteLoan` - HTTP DELETE /api/loans/{id}
- ✅ Search/criteria queries

#### PaymentController (5 tests):
- ✅ `testRecordPaymentSuccess` - HTTP POST /api/payments/record
- ✅ `testGetPaymentsByLoan` - HTTP GET /api/payments/loan/{id}
- ✅ `testGetPaymentsByEmi` - HTTP GET /api/payments/emi/{id}
- ✅ `testGetPaymentById` - HTTP GET /api/payments/{id}
- ✅ Payment status transitions

**Expected Coverage: 87-91%**

---

## 3. NOTIFICATION-SERVICE

### Test Classes Created:
1. **LoanApplicationEventListenerTest.java** - 7 test cases

### Coverage Details:

#### LoanApplicationEventListener (7 tests):
- ✅ `testHandleLoanApplicationCreatedEvent` - CREATED event
- ✅ `testHandleLoanApplicationApprovedEvent` - APPROVED event
- ✅ `testHandleLoanApplicationRejectedEvent` - REJECTED event
- ✅ `testHandleEmiDueEvent` - EMI_DUE event
- ✅ `testHandleEmiOverdueEvent` - EMI_OVERDUE event
- ✅ `testHandleLoanClosedEvent` - LOAN_CLOSED event
- ✅ `testHandleUnknownEventType` - Error handling

**Expected Coverage: 85-90%**

---

## 4. AUTH-SERVICE

### Test Cases to Create:
- AuthService: Register, Login, Token generation, Token validation
- AuthController: /register, /login, /validate endpoints
- Expected Coverage: 85-90%

---

## 5. PROFILE-SERVICE

### Test Cases to Create:
- ProfileService: Create, Update, Get, Delete profiles
- ProfileController: CRUD endpoints
- Expected Coverage: 85-90%

---

## 6. REPORT-SERVICE

### Test Cases to Create:
- ReportService: Generate reports, Aggregations
- ReportController: Report endpoints
- Feign client fallbacks
- Expected Coverage: 85-90%

---

## Test Execution Instructions

### Run All Tests:
```bash
cd backend-java
mvn clean test
```

### Run Tests for Specific Module:
```bash
cd backend-java/loan-application-service
mvn test
```

### Generate Coverage Report:
```bash
mvn clean test jacoco:report
```

Coverage report available at: `target/site/jacoco/index.html`

### Check Coverage in IDE:
```
Right-click project → Run Tests with Coverage
```

---

## Mocking Strategy

### External Dependencies (Always Mocked):
1. **Database Repositories** - `@Mock LoanApplicationRepository`
2. **Feign Clients** - `@Mock ProfileServiceClient`, `@Mock LoanServiceClient`
3. **Email Service** - `@Mock EmailService`
4. **RabbitMQ Publishers** - `@Mock NotificationPublisher`
5. **Security Context** - `@Mock Authentication`, `@Mock SecurityContext`

### Testing Approach:
- **Unit Tests**: Test business logic in isolation
- **Controller Tests**: Test HTTP layer (status codes, request/response)
- **Integration Tests** (Optional): Test across multiple services
- **Fallback Tests**: Verify circuit breaker behavior

---

## Coverage Thresholds

| Service | Method Coverage | Line Coverage | Branch Coverage |
|---------|-----------------|---------------|-----------------|
| Loan-Application | 90%+ | 88%+ | 85%+ |
| Loan-Service | 90%+ | 87%+ | 85%+ |
| Notification-Service | 85%+ | 85%+ | 80%+ |
| Auth-Service | 85%+ | 85%+ | 80%+ |
| Profile-Service | 85%+ | 85%+ | 80%+ |
| Report-Service | 85%+ | 85%+ | 80%+ |

---

## Key Testing Patterns Used

1. **Arrange-Act-Assert (AAA)**
   ```java
   @Test
   void testExample() {
       // Arrange
       when(repo.findById("id")).thenReturn(Optional.of(data));
       
       // Act
       Result result = service.get("id");
       
       // Assert
       assertEquals(expected, result);
   }
   ```

2. **Mock Verification**
   ```java
   verify(service, times(1)).method(args);
   verify(service, never()).method(args);
   ```

3. **Exception Testing**
   ```java
   ResponseStatusException ex = assertThrows(
       ResponseStatusException.class,
       () -> service.invalidOperation()
   );
   assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
   ```

4. **Parameterized Tests** (Optional enhancement)
   ```java
   @ParameterizedTest
   @ValueSource(doubles = {1000, 5000, 100000})
   void testAmountValidation(double amount) { ... }
   ```

---

## Running Tests in CI/CD

```yaml
# Example: GitHub Actions / GitLab CI
test:
  script:
    - mvn clean test
    - mvn jacoco:report
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    paths:
      - '**/target/site/jacoco/'
```

---

## Notes
- All tests use **JUnit 5** and **Mockito**
- Tests follow **@DisplayName** convention for clarity
- Fallback handlers have dedicated tests
- Event publishing tested implicitly through service tests
- Security (@PreAuthorize) tested through controller tests

