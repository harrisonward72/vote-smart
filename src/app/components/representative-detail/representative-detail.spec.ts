import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RepresentativeDetail } from './representative-detail';

describe('RepresentativeDetail', () => {
  let component: RepresentativeDetail;
  let fixture: ComponentFixture<RepresentativeDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RepresentativeDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RepresentativeDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
