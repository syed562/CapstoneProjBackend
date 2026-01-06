import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EmiScheduleComponent } from './emi-schedule/emi-schedule.component';

const routes: Routes = [
  { path: '', component: EmiScheduleComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EmiRoutingModule { }
