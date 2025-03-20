import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ApiResponseModelRepos} from '../Model/apiResponseModelRepos';

@Injectable({
  providedIn: 'root'
})
export class ApiRepo {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/repo';

  //get all existing repos
  getRepos() {
    return this.http.get<ApiResponseModelRepos>(this.baseUrl + "/get");
  }
}
