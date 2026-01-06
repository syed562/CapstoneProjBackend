import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ReportService } from '../../../core/services/report.service';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';

@Component({
  selector: 'app-reports-dashboard',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
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

  // Chart configurations
  loanStatusChartConfig: ChartConfiguration<'pie'> | null = null;
  applicationStatusChartConfig: ChartConfiguration<'pie'> | null = null;
  dashboardChartConfig: ChartConfiguration<'bar'> | null = null;

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
        this.initializeCharts();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load reports.';
        this.loading = false;
      }
    });
  }

  initializeCharts(): void {
    this.loanStatusChartConfig = this.createLoanStatusChart();
    this.applicationStatusChartConfig = this.createApplicationStatusChart();
    this.dashboardChartConfig = this.createDashboardChart();
  }

  createLoanStatusChart(): ChartConfiguration<'pie'> {
    const statusDist = this.loanStatus?.statusDistribution || {};
    const labels = Object.keys(statusDist);
    const data = Object.values(statusDist);

    return {
      type: 'pie',
      data: {
        labels: labels.map(l => this.capitalizeStatus(l)),
        datasets: [{
          data: data as number[],
          backgroundColor: [
            '#FF6384',
            '#36A2EB',
            '#FFCE56',
            '#4BC0C0'
          ],
          borderColor: '#fff',
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: { size: 12 }
            }
          },
          title: {
            display: true,
            text: 'Loan Status Distribution',
            font: { size: 16, weight: 'bold' }
          }
        }
      }
    };
  }

  createApplicationStatusChart(): ChartConfiguration<'pie'> {
    const appStats = this.comprehensive?.applications || {};
    const labels = ['Pending', 'Under Review', 'Approved', 'Rejected'];
    const data = [
      appStats.pending || 0,
      appStats.underReview || 0,
      appStats.approved || 0,
      appStats.rejected || 0
    ];

    return {
      type: 'pie',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: [
            '#FFA500',
            '#FFB6C1',
            '#90EE90',
            '#FF6B6B'
          ],
          borderColor: '#fff',
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: { size: 12 }
            }
          },
          title: {
            display: true,
            text: 'Application Status Distribution',
            font: { size: 16, weight: 'bold' }
          }
        }
      }
    };
  }

  createDashboardChart(): ChartConfiguration<'bar'> {
    return {
      type: 'bar',
      data: {
        labels: ['Total', 'Pending', 'Approved', 'Rejected'],
        datasets: [{
          label: 'Count',
          data: [
            this.dashboard?.totalLoans || 0,
            this.dashboard?.pendingLoans || 0,
            this.dashboard?.approvedLoans || 0,
            this.dashboard?.rejectedLoans || 0
          ],
          backgroundColor: [
            '#36A2EB',
            '#FFA500',
            '#90EE90',
            '#FF6B6B'
          ],
          borderColor: [
            '#1E88E5',
            '#FF8C00',
            '#4CAF50',
            '#E53935'
          ],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        indexAxis: 'x',
        plugins: {
          legend: {
            display: false
          },
          title: {
            display: true,
            text: 'Loan Overview',
            font: { size: 16, weight: 'bold' }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1
            }
          }
        }
      }
    };
  }

  capitalizeStatus(status: string): string {
    return status
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  getStatusCount(status: string): number {
    return this.loanStatus?.statusDistribution?.[status] ?? 0;
  }
}
