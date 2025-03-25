import {jwtDecode} from 'jwt-decode';
import {Injectable, signal} from '@angular/core';
import {ApiAuth} from '../../api/apiAuth';
import {ToastrService} from 'ngx-toastr';
import {ResourceService} from './resource.service';
import {ApiResource} from '../../api/apiResource';

@Injectable({
  providedIn: 'root'
})

export class AuthService {
  constructor(private apiAuth: ApiAuth,
              private toastr: ToastrService,
              private apiResource: ApiResource) {
  }

  username = signal<string>("");

  logIn(username: string, password: string) {
    this.apiAuth.login(username, password).subscribe(
      data => {
        localStorage.setItem('authToken', data.token);
        let username = this.decodeToke(localStorage.getItem("authToken"))
        if (typeof username === "string") {
          this.username.set(username);
        }
        this.toastr.success("login successful")
      },
      error => {
        this.toastr.error(error.error.error, "Login failed: ")
      }
    )
  }

  decodeToke(storage: string | null) {
    if (storage) {
      const token = jwtDecode(storage)
      return token.sub
    }
    return null

  }
}
