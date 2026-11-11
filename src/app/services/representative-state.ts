import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class RepresentativeState {

  private selectedRep = signal<any>(null);
  private selectedCandidate = signal<any>(null);
  private cachedCivicData = signal<any>(null);
  private cachedAddress = signal<string>('');

  setRep(rep: any): void {
    this.selectedRep.set(rep);
  }

  getRep(): any {
    return this.selectedRep();
  }

  setCandidate(candidate: any): void {
    this.selectedCandidate.set(candidate);
  }

  getCandidate(): any {
    return this.selectedCandidate();
  }

  setCivicData(data: any, address: string): void {
    this.cachedCivicData.set(data);
    this.cachedAddress.set(address);
  }

  getCivicData(): any {
    return this.cachedCivicData();
  }

  getCachedAddress(): string {
    return this.cachedAddress();
  }
}
