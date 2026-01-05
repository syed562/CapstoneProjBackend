import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanApplicationsComponent } from './loan-applications.component';

describe('LoanApplicationsComponent', () => {
  let component: LoanApplicationsComponent;
  let fixture: ComponentFixture<LoanApplicationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanApplicationsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoanApplicationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
