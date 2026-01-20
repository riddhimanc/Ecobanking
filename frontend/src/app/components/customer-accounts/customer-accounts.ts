import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router} from '@angular/router';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl:'./customer-accounts.html',
  styleUrl: './customer-accounts.css'
})
export class CustomerAccountsComponent implements OnInit {
  customer: any;
  accounts: any[] = [];

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const customerId = Number(this.route.snapshot.paramMap.get('id'));

    this.api.getCustomer(customerId).subscribe(c => {
      this.customer = c;
    });

    this.api.getCustomerAccounts(customerId).subscribe(a => {
      this.accounts = a;
    });
  }

  openAccount(accountNo: string) {
    this.router.navigate(
      ['/account', accountNo],
      { queryParams: { customerId: this.customer.id } }
    );
  }

  goBackToDirectory() {
    this.router.navigate(['/']);
  }
}
