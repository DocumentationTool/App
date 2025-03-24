import {jwtDecode} from 'jwt-decode';
import {Injectable, signal} from '@angular/core';
import {ApiAuth} from '../../api/apiAuth';
import {ToastrService} from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})

export class AuthService {
  constructor(private apiAuth: ApiAuth,
              private toastr: ToastrService) {
  }

  isAdmin = signal<boolean>(false);
  username = signal<string>("");

  logAdminIn() {
    this.apiAuth.login("admin", "123").subscribe(
      data => {
        localStorage.setItem('authToken', data.token);
        let username = this.decodeToke(localStorage.getItem("authToken"))
        if (typeof username === "string") {
          this.username.set(username);
          this.isAdmin.set(true);
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
      console.log(token)
      return token.sub
    }
    return null

  }

  logUserIn() {
    this.isAdmin.set(false);
    //ToDo: ausbauen:
    this.getTestUser();
  }

  getTestUser() {
    this.apiAuth.testLogin().subscribe(response => {
        console.log(response)
      },
      (error) => {
        console.error("Fehler Bei testLogin ", error)
      });
  }

}
