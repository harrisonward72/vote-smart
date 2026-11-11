import { ApiService } from './../../services/api.service';
import { Component, OnInit, signal, inject } from '@angular/core';
import { RepresentativeState } from '../../services/representative-state';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, SlicePipe } from '@angular/common';

@Component({
  selector: 'app-representative-detail',
  imports: [SlicePipe, CommonModule],
  templateUrl: './representative-detail.html',
  styleUrl: './representative-detail.css',
})
export class RepresentativeDetail implements OnInit{

  rep = signal<any>(null);
  address: string = '';
  summary = signal<any>(null);
  summaryLoading = signal<boolean>(false);
  summaryError = signal<string>('');
  congressData = signal<any>(null);
  news = signal<any[]>([]);
  newsLoading = signal<boolean>(false);
  fromTab: string = 'representatives';

  private repState = inject(RepresentativeState);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService)

  ngOnInit(): void {
    const repData = this.repState.getRep();
    this.address = this.route.snapshot.queryParamMap.get('address') ?? '';
    this.fromTab = this.route.snapshot.queryParamMap.get('fromTab') ?? 'representatives';
    if (!repData) {
      //if no rep in state direct user back home
      this.router.navigate(['/']);
      return;
    }
    this.rep.set(repData);
    this.loadSummary(repData);
    this.loadCongressionalData(repData);
    this.loadNews(repData);
  }

  loadSummary(rep: any): void {
    this.summaryLoading.set(true);
    this.summaryError.set('');

    this.apiService.getPoliticianSummary(rep.name, rep.role, rep.party, rep.jurisdiction).subscribe({
      next: (data) => {
        this.summary.set(data);
        this.summaryLoading.set(false);
      },
      error: (err) => {
        console.log('Summary error: ', err);
        this.summaryError.set('Could not load summary. Please try again.');
        this.summaryLoading.set(false);
      }
    });
  }

  loadCongressionalData(rep: any): void {
    //only fetching federal legislators for now
    if(rep.jurisdiction !== 'United States') return;

    this.apiService.getCongressionalData(rep.name).subscribe({
      next: (data) => {
        if (data.found) {
          this.congressData.set(data);
          console.log('Congressional data: ', data);
        }
      },
      error: (err) => {
        console.log('Congressional data error:', err);
        }
    });
  }

  scrollTo(sectionId: string): void {
    const element = document.getElementById(sectionId);
    if(element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start'
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard'], {
      queryParams: { address: this.address, tab: this.fromTab }
    });
  }

  calculateAge(birthDate: string): string {
    if (!birthDate) return "N/A";
    const birth = new Date(birthDate);
    const today = new Date();
    const age = today.getFullYear() - birth.getFullYear();
    return age.toString();
  }

  //helper method -- claud sometimes returns a single string instead of an array for certain fields
  asArray(val: any): any[] {
    return Array.isArray(val) ? val : [];
  }

  loadNews(rep: any): void {
    this.newsLoading.set(true);

    this.apiService.getNews(rep.name, rep.role, rep.jurisdiction).subscribe({
      next: (data) => {
        this.news.set(data);
        this.newsLoading.set(false);
        console.log('News loaded:', data);
      },
      error: (err) => {
        console.log('News error:', err);
        this.newsLoading.set(false);
      }
    });
  }
}


