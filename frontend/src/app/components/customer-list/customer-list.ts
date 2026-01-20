import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-list.html',
  styleUrl: './customer-list.css'
})
export class CustomersComponent implements OnInit {
  customers: any[] = [];

  constructor(private api: ApiService, private router: Router) {}

  ngOnInit() {
    this.api.getCustomers().subscribe(r => {
  this.customers = r;
});

  }

  openCustomer(id: number) {
    this.router.navigate(['/customer', id]);
  }

   logout() {
    window.location.href = 'http://localhost:8080/logout';
  }
}
