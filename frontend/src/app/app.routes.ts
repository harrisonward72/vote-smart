import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { Dashboard } from './components/dashboard/dashboard';
import { RepresentativeDetail } from './components/representative-detail/representative-detail';
import { Election } from './components/election/election';
import { CandidateDetail } from './components/candidate-detail/candidate-detail';

export const routes: Routes = [
    {path: '', component: Home},
    {path: 'dashboard', component: Dashboard},
    {path: 'representative/:id', component: RepresentativeDetail},
    {path: 'election', component: Election},
    {path: 'candidate/:name', component: CandidateDetail},
    {path: '**', redirectTo: ''}
];
