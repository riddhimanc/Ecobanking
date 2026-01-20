import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './opportunity-form.html',
  styleUrl:'./opportunity-form.css'
})
export class OpportunityFormComponent implements OnInit {

  accountNo!: string;
  customerId!: number;

  products: any[] = [];

  opportunity: any = {
    id: null,
    productId: null,
    assignedStaff: '',
    status: 'NEW'
  };

  isEdit = false;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.accountNo = this.route.snapshot.paramMap.get('acc')!;
  this.customerId = Number(this.route.snapshot.queryParamMap.get('customerId'));
  const oppId = this.route.snapshot.queryParamMap.get('oppId');

  // products
  this.api.getProducts().subscribe(p => this.products = p);

  if (oppId) {
    this.isEdit = true;
    this.api.getOpportunity(Number(oppId)).subscribe(o => {
      this.opportunity = o;
    });
  }
  }

save() {
  const payload = {
    productId: this.opportunity.productId,
    assignedStaff: this.opportunity.assignedStaff,
    status: this.opportunity.status
  };

  const request$ = this.isEdit
    ? this.api.updateOpportunity(this.opportunity.id, payload)
    : this.api.createOpportunity(this.accountNo, payload);

  request$.subscribe(() => {
    this.router.navigate(['/account', this.accountNo]);
  });
}



  cancel() {
    this.router.navigate(['/account', this.accountNo], {
      queryParams: { customerId: this.customerId }
    });
  }
}
