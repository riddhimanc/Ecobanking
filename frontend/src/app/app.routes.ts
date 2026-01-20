import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { CustomerAccountsComponent } from './components/customer-accounts/customer-accounts';
import { AccountDetailsComponent } from './components/account-details/account-details';
import { CustomersComponent } from './components/customer-list/customer-list';
import { OpportunityFormComponent } from './components/opportunity-form/opportunity-form';
import { AuthGuard } from './services/auth.guard';


export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: CustomersComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'customer/:id',
    component: CustomerAccountsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'account/:acc',
    component: AccountDetailsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'account/:acc/opportunity',
    component: OpportunityFormComponent,
    canActivate: [AuthGuard]
  },

  { path: '**', redirectTo: '' }
];
