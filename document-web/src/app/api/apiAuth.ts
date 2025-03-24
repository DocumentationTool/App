import {Injectable} from '@angular/core';
import {TestLoginResponseModel} from '../Model/TestLoginResponseModel';
import {HttpClient} from '@angular/common/http';
import {ApiResponseLogin} from '../Model/apiResponseLogin';

@Injectable({
  providedIn: 'root'
})
export class ApiAuth {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/auth';

  testLogin() {
    return this.http.get<TestLoginResponseModel>(this.baseUrl + "/login");
  }

  login(userId: string, password: string) {
    const params = {userId, password}
    return this.http.post<ApiResponseLogin>(this.baseUrl + "/login", params)
  }

  logout() {
    return this.http.get(this.baseUrl + "/logout");
  }
}
