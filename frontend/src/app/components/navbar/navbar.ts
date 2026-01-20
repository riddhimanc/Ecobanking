import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-navbar',
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent implements OnInit {

  user: any;

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.auth.getCurrentUser().subscribe(u => this.user = u);
  }

  logout() {
    this.auth.logout();
  }
}
