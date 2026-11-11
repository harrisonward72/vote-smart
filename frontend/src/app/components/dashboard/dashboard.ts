import { CommonModule } from '@angular/common';
import { Component, signal, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { RepresentativeState } from '../../services/representative-state';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {

  address: string = '';
  civicData = signal<any>(null);
  loading: boolean = false;
  error: string = '';
  activeTab: string = 'representatives';

  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);
  private router = inject(Router);
  private repState = inject(RepresentativeState);

  ngOnInit(): void {
    const addr = this.route.snapshot.queryParamMap.get('address');
    const tab = this.route.snapshot.queryParamMap.get('tab');
    this.address = addr ?? '';

    if (tab) {
      this.activeTab = tab;
    }

    if (!this.address) return;

    const cached = this.repState.getCivicData();
    const cachedAddr = this.repState.getCachedAddress();

    if (cached && cachedAddr === this.address) {
      console.log('Using cached civic data');
      this.civicData.set(cached);
    } else {
      this.loadCivicData();
    }
  }

  setTab(tab: string): void {
    this.activeTab = tab;
    this.router.navigate([], {
      queryParams: { address: this.address, tab: tab },
      queryParamsHandling: 'merge'
    });
  }

  goToRep(rep: any): void {
    this.repState.setRep(rep);
    this.router.navigate(['/representative', rep.name], {
      queryParams: { address: this.address, fromTab: this.activeTab }
    });
  }

  goToCandidate(candidate: any): void {
    console.log('activeTab when navigating:', this.activeTab);

    if (candidate.incumbentStatus === 'Incumbent') {
      const repData = this.civicData().representatives.find(
        (r: any) => r.name === candidate.name
      );
      if (repData) {
        this.repState.setRep(repData);
        this.router.navigate(['/representative', candidate.name], {
          queryParams: { address: this.address, fromTab: this.activeTab }
        });
        return;
      }
    }
    this.repState.setCandidate(candidate);
    this.router.navigate(['/candidate', candidate.name], {
      queryParams: { address: this.address, fromTab: this.activeTab }
    });
  }

  loadCivicData(): void {
    this.loading = true;
    this.error = '';

    this.apiService.lookupAddress(this.address).subscribe({
      next: (data) => {
        this.civicData.set(data);
        this.repState.setCivicData(data, this.address);
        console.log('Civic data loaded and cached');
        this.loading = false;
      },
      error: (err) => {
        console.log('Error:', err);
        this.error = 'Failed to load data. Please try again.';
        this.loading = false;
      }
    });
  }
}