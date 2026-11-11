import { CommonModule } from '@angular/common';
import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [CommonModule, FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

  street: string = '';
  city: string = '';
  state: string = '';
  zip: string = '';
  errorMessage: string = '';

  private router = inject(Router);

  onSubmit() {
    console.log('State value on submit: ', this.state)
    if(!this.street || !this.city || !this.state || !this.zip) {
      this.errorMessage = 'Please fill in all address fields correctly.';
      return;
    }

    const formattedAddress = `${this.street.trim()}, ${this.city.trim()}, ${this.state.toUpperCase().trim()} ${this.zip.trim()}`;
    console.log('Formatted address: ', formattedAddress);

    this.router.navigate(['/dashboard'],  {
      queryParams: { address: formattedAddress, state: this.state.toUpperCase() }
    });
    
  }

}
