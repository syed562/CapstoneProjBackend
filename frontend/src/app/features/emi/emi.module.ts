import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmiRoutingModule } from './emi-routing.module';
import { EmiScheduleComponent } from './emi-schedule/emi-schedule.component';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    EmiRoutingModule,
    EmiScheduleComponent  // Import standalone component
  ]
})
export class EmiModule { }
