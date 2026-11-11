import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ApiService {

  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  checkHealth(): Observable<string> {
    return this.http.get(`${this.baseUrl}/health`, {responseType: 'text'});
  }
  
  lookupAddress(address: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/civic/lookup`, {
      params: {address: address}
    });
  }

  getPoliticianSummary(name: string, role: string, party: string, jurisdiction: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/politician/summary`, {
      params: { name, role, party, jurisdiction }
    });
  }

  getCongressionalData(name: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/politician/congressional-data`, {
      params: { name }
    });
  }

  getNews(name: string, role: string, jurisdiction: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/politician/news`, {
      params: { name, role, jurisdiction }
    });
  }

  getChallengerSummary(name: string, party: string, office: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/politician/challenger-summary`, {
      params: { name, party, office }
    });
  }

  getChallengerNews(name: string, office: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/politician/challenger-news`, {
      params: { name, office }
    });
  }

}
