import { Component, signal, OnInit } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { ApiService } from './services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css',
  standalone: true
})
export class App implements OnInit {
  protected readonly title = signal('voteSmartFrontEnd');

  status = signal('');

  constructor(private apiService: ApiService) {}

  ngOnInit() {
  this.apiService.checkHealth().subscribe({
    next: (response) => {
      console.log('Response received:', response);
      this.status.set(response);
    },
    error: (err) => {
      console.log('Error:', err);
    }
  });
}
}
