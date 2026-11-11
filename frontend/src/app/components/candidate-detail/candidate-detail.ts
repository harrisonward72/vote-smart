import { ApiService } from './../../services/api.service';
import { Component, OnInit, signal, inject } from '@angular/core';
import { RepresentativeState } from '../../services/representative-state';
import { ActivatedRoute, Router } from '@angular/router';
import { SlicePipe } from '@angular/common';

@Component({
  selector: 'app-candidate-detail',
  imports: [SlicePipe],
  templateUrl: './candidate-detail.html',
  styleUrl: './candidate-detail.css',
})
export class CandidateDetail implements OnInit{

  candidate = signal<any>(null);
  summary = signal<any>(null);
  news = signal<any[]>([]);
  summaryLoading = signal<boolean>(false);
  newsLoading = signal<boolean>(false);
  summaryError = signal<string>('');
  address: string = '';
  fromTab: string = 'election';

  private repState = inject(RepresentativeState)
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);

  ngOnInit(): void {
    const candidateData = this.repState.getCandidate();
    this.address = this.route.snapshot.queryParamMap.get('address') ?? '';
    this.fromTab = this.route.snapshot.queryParamMap.get('fromTab') ?? 'election';

    if(!candidateData) {
      this.router.navigate(['/']);
      return;
    }

    this.candidate.set(candidateData);
    this.loadSummary(candidateData);
    this.loadNews(candidateData);

  }

  loadSummary(candidate: any): void {
    this.summaryLoading.set(true);
    this.summaryError.set('');

    this.apiService.getChallengerSummary(candidate.name, candidate.party, candidate.incumbentStatus === 'Incumbent' ? candidate.name : 'office')
    .subscribe({
      next: (data) => {
        this.summary.set(data);
        this.summaryLoading.set(false);
      },
      error: (err) => {
        console.log('Challenger summary error:', err);
        this.summaryError.set('Could not load profile. Please try again');
        this.summaryLoading.set(false);
      }
    });
  }

  loadNews(candidate: any): void {
    this.newsLoading.set(true);
    
    this.apiService.getChallengerNews(candidate.name, candidate.party)
    .subscribe({
      next: (data) => {
        this.news.set(data);
        this.newsLoading.set(false);
      },
      error: (err) => {
        console.log('Challenger news error:', err);
        this.newsLoading.set(false);
      }
    });
  }

  scrollTo(sectionId: string): void {
    const element = document.getElementById(sectionId);
    if(element) {
      element.scrollIntoView({behavior: 'smooth', block: 'start'});
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard'], {
      queryParams: { address: this.address, tab: this.fromTab }
    });
  }

  asArray(val: any): any[] {
    return Array.isArray(val) ? val : [];
  }

}
