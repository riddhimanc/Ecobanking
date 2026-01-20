import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-details.html',
  styleUrl: './account-details.css'
})
export class AccountDetailsComponent implements OnInit {

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

 account: any;
  opportunities: any[] = [];
  transactions: any[] = [];
  customerId!: number;

  ngOnInit() {
    const accountNo = this.route.snapshot.paramMap.get('acc')!;
        this.customerId = Number(this.route.snapshot.queryParamMap.get('customerId'));

  this.api.getAccountDetails(accountNo).subscribe({
    next: data => {
      this.account = data.account;
      this.transactions = data.transactions;
      this.opportunities = data.opportunities;
      this.customerId = data.account.customer.id;
    },
    error: err => {
      console.error(err);
      alert('Failed to load account details');
    }
  });
  }
  generate() {
  if (!this.account) return;

  this.api.generateOpportunities(this.account.accountNo).subscribe({
    next: res => {
      this.opportunities = res.opportunities;
      alert(res.message);
    },
    error: () => {
      alert('Failed to generate opportunities');
    }
  });
}

goBack() {
  this.router.navigate(['/customer', this.customerId]);
}

addManually() {
  this.router.navigate(
    ['/account', this.account.accountNo, 'opportunity'],
    { queryParams: { customerId: this.customerId } }
  );
}

editOpportunity(oppId: number) {
  this.router.navigate(
    ['/account', this.account.accountNo, 'opportunity'],
    { queryParams: { customerId: this.customerId, oppId } }
  );
}


}
