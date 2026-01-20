import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';


@Component({
  standalone: true,
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  constructor(private auth: AuthService) {}
  login() { this.auth.login(); }
}
