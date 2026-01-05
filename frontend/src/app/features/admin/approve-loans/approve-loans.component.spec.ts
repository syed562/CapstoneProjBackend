import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApproveLoansComponent } from './approve-loans.component';

describe('ApproveLoansComponent', () => {
  let component: ApproveLoansComponent;
  let fixture: ComponentFixture<ApproveLoansComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApproveLoansComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ApproveLoansComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
