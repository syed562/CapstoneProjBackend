import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserAdminService, AdminUserView } from '../../../core/services/user-admin.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  users: AdminUserView[] = [];
  loading = false;
  error: string | null = null;
  saving: Record<string, boolean> = {};

  roles = ['ADMIN', 'LOAN_OFFICER', 'CUSTOMER'];
  statuses = ['ACTIVE', 'INACTIVE', 'SUSPENDED'];
  currentUserId: string | null = null;

  constructor(
    private userAdminService: UserAdminService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const me = this.authService.currentUserValue;
    this.currentUserId = me?.userId || null;
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;
    this.userAdminService.listUsers().subscribe({
      next: (data) => {
        this.users = data || [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load users';
        this.loading = false;
      }
    });
  }

  onRoleChange(user: AdminUserView, role: string): void {
    this.saving[user.id] = true;
    this.userAdminService.updateRole(user.id, role).subscribe({
      next: (updated) => {
        user.role = updated.role;
        this.saving[user.id] = false;
      },
      error: () => {
        this.error = 'Could not update role';
        this.saving[user.id] = false;
      }
    });
  }

  toggleStatus(user: AdminUserView): void {
    const nextStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.saving[user.id] = true;
    this.userAdminService.updateStatus(user.id, nextStatus).subscribe({
      next: (updated) => {
        user.status = updated.status;
        this.saving[user.id] = false;
      },
      error: () => {
        this.error = 'Could not update status';
        this.saving[user.id] = false;
      }
    });
  }

  deleteUser(user: AdminUserView): void {
    if (!confirm(`Delete user ${user.username}? This cannot be undone.`)) {
      return;
    }
    if (this.currentUserId && user.id === this.currentUserId) {
      this.error = 'You cannot delete your own account';
      return;
    }
    this.saving[user.id] = true;
    this.userAdminService.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== user.id);
        this.saving[user.id] = false;
      },
      error: () => {
        this.error = 'Could not delete user';
        this.saving[user.id] = false;
      }
    });
  }
}
