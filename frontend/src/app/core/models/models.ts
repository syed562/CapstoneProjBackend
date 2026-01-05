export interface User {
  userId: string;
  username: string;
  email: string;
  role: 'CUSTOMER' | 'ADMIN' | 'LOAN_OFFICER';
  firstName?: string;
  lastName?: string;
  isActive?: boolean;
  profileCompleted?: boolean;
}

export interface Profile {
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  kycStatus?: string;
  creditScore?: string;
  annualIncome?: string;
  totalLiabilities?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token?: string;
  accessToken?: string;
  jwt?: string;
  userId: string;
  username: string;
  role: string;
  message?: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface LoanApplication {
  id: string;
  userId: string;
  amount: number;
  tenure: number;
  loanType: 'PERSONAL' | 'HOME' | 'AUTO' | 'EDUCATIONAL' | 'HOME_LOAN';
  purpose: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  appliedDate: string;
  approvedDate?: string;
  rejectionReason?: string;
  interestRate?: number;
}

export interface Loan {
  id: string;
  applicationId: string;
  userId: string;
  amount: number;
  tenure: number;
  interestRate: number;
  loanType: string;
  status: 'ACTIVE' | 'CLOSED' | 'DEFAULTED';
  startDate: string;
  endDate: string;
  emiAmount: number;
  totalAmount: number;
  paidAmount: number;
  remainingAmount: number;
}

export interface EMISchedule {
  emiNumber: number;
  dueDate: string;
  amount: number;
  principal: number;
  interest: number;
  remainingBalance: number;
  status: 'PENDING' | 'PAID' | 'OVERDUE';
  paidDate?: string;
}

export interface Report {
  totalLoans: number;
  activeLoans: number;
  closedLoans: number;
  totalDisbursed: number;
  totalCollected: number;
  pendingApplications: number;
}

export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
}
