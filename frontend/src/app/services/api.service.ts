import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private API = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /* ---------------- HOME (home.html) ---------------- */
  getCustomers() {
    return this.http.get<any[]>(
      `${this.API}/customers`,
      { withCredentials: true }
    );
  }

  /* ---------------- CUSTOMER DASHBOARD ---------------- */
  getCustomer(customerId: number) {
    return this.http.get<any>(
      `${this.API}/customers/${customerId}`,
      { withCredentials: true }
    );
  }

  getCustomerAccounts(customerId: number) {
    return this.http.get<any[]>(
      `${this.API}/customers/${customerId}/accounts`,
      { withCredentials: true }
    );
  }

  // /* ---------------- ACCOUNT DETAILS ---------------- */
  // getAccountDetails(customerId: number, accountNo: string) {
  //   return forkJoin({
  //     accounts: this.getCustomerAccounts(customerId),
  //     pendingOpps: this.getPendingOpportunities(customerId),
  //     fulfilledOpps: this.getFulfilledOpportunities(customerId)
  //   });
  // }

  getAccountDetails(accountNo: string) {
  return this.http.get<any>(
    `${this.API}/account/details`,
    {
      params: { accountNumber: accountNo },
      withCredentials: true
    }
  );
}


  /* ---------------- OPPORTUNITIES ---------------- */
  getPendingOpportunities(customerId: number) {
    return this.http.get<any[]>(
      `${this.API}/opportunities/pending?customerId=${customerId}`,
      { withCredentials: true }
    );
  }

  getFulfilledOpportunities(customerId: number) {
    return this.http.get<any[]>(
      `${this.API}/opportunities/fulfilled?customerId=${customerId}`,
      { withCredentials: true }
    );
  }

    // CREATE
  // createOpportunity(payload: any) {
  //   return this.http.post(
  //     `${this.API}/opportunities`,
  //     payload,
  //     { withCredentials: true }
  //   );
  // }

  createOpportunity(accountNo: string, payload: any) {
  return this.http.post(
    `${this.API}/account/${accountNo}/opportunity`,
    payload,
    { withCredentials: true }
  );
}



  // UPDATE
  updateOpportunity(id: number, payload: any) {
    return this.http.put(
      `${this.API}/opportunities/${id}`,
      payload,
      { withCredentials: true }
    );
  }

  deleteOpportunity(id: number) {
    return this.http.delete(
      `${this.API}/opportunities/${id}`,
      { withCredentials: true }
    );
  }

  // PRODUCTS
  getProducts() {
  return this.http.get<any[]>(
    `${this.API}/products`,
    { withCredentials: true }
  );
}

// GET OPPORTUNITY (EDIT)
getOpportunity(id: number) {
  return this.http.get<any>(
    `${this.API}/opportunities/${id}`,
    { withCredentials: true }
  );
}


generateOpportunities(accountNo: string) {
  return this.http.post<any>(
    `${this.API}/account/${accountNo}/opportunities/generate`,
    {},
    { withCredentials: true }
  );
}


}
