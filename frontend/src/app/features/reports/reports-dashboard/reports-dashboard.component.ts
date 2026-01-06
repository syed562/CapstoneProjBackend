import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ReportService } from '../../../core/services/report.service';

@Component({
  selector: 'app-reports-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports-dashboard.component.html',
  styleUrl: './reports-dashboard.component.scss'
})
export class ReportsDashboardComponent implements OnInit {
  loading = false;
  error: string | null = null;

  loanStatus: any = null;
  comprehensive: any = null;
  customerSummary: any[] = [];
  dashboard: any = null;

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading = true;
    this.error = null;
    forkJoin({
      loanStatus: this.reportService.getLoanStatus(),
      comprehensive: this.reportService.getComprehensive(),
      customerSummary: this.reportService.getCustomerSummary(),
      dashboard: this.reportService.getDashboard()
    }).subscribe({
      next: (res) => {
        this.loanStatus = res.loanStatus;
        this.comprehensive = res.comprehensive;
        this.customerSummary = res.customerSummary || [];
        this.dashboard = res.dashboard;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load reports.';
        this.loading = false;
      }
    });
  }

  getStatusCount(status: string): number {
    return this.loanStatus?.statusDistribution?.[status] ?? 0;
  }
}
