import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AuditComponent} from './audit.component';

describe('AdminComponent', () => {
  let component: AuditComponent;
  let fixture: ComponentFixture<AuditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AuditComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
