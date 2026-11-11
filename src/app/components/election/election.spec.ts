import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Election } from './election';

describe('Election', () => {
  let component: Election;
  let fixture: ComponentFixture<Election>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Election]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Election);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
