import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { UserAdminService, AdminUserView } from '../../../core/services/user-admin.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  users: AdminUserView[] = [];
  loading = false;
  error: string = '';
  success: string = '';
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

  get activeCount(): number {
    return (this.users || []).filter(u => u.status === 'ACTIVE').length;
  }

  get inactiveCount(): number {
    return (this.users || []).filter(u => u.status !== 'ACTIVE').length;
  }

  statusIcon(user: AdminUserView): string {
    return user.status === 'ACTIVE' ? 'pause_circle' : 'play_circle';
  }

  statusActionLabel(user: AdminUserView): string {
    return user.status === 'ACTIVE' ? 'Deactivate' : 'Activate';
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';
    this.success = '';
    this.userAdminService.listUsers().subscribe({
      next: (data) => {
        this.users = data || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || err?.message || 'Failed to load users';
        this.loading = false;
      }
    });
  }

  onRoleChange(user: AdminUserView, role: string): void {
    this.saving[user.id] = true;
    this.error = '';
    this.success = '';
    this.userAdminService.updateRole(user.id, role).subscribe({
      next: (updated) => {
        user.role = updated.role;
        this.saving[user.id] = false;
        this.success = 'Role updated successfully';
        setTimeout(() => { this.success = ''; }, 3000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Could not update role';
        this.saving[user.id] = false;
      }
    });
  }

  toggleStatus(user: AdminUserView): void {
    const nextStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.saving[user.id] = true;
    this.error = '';
    this.success = '';
    this.userAdminService.updateStatus(user.id, nextStatus).subscribe({
      next: (updated) => {
        user.status = updated.status;
        this.saving[user.id] = false;
        this.success = `User status changed to ${nextStatus}`;
        setTimeout(() => { this.success = ''; }, 3000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Could not update status';
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
    this.error = '';
    this.success = '';
    this.userAdminService.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== user.id);
        this.saving[user.id] = false;
        this.success = 'User deleted successfully';
        setTimeout(() => { this.success = ''; }, 3000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Could not delete user';
        this.saving[user.id] = false;
      }
    });
  }
}
