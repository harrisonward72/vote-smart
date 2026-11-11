import { TestBed } from '@angular/core/testing';

import { RepresentativeState } from './representative-state';

describe('RepresentativeState', () => {
  let service: RepresentativeState;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RepresentativeState);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
