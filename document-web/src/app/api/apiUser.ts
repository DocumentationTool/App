import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiUser {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/user';


  removeUser(repoId: string, userId: string) {
    const params = {repoId, userId}
    return this.http.put(this.baseUrl + "/remove", {params});
  }

  addUser(repoId: string, userId: string, password: string) {
    const params = {repoId, userId, password}
    return this.http.put(this.baseUrl + "/add", {params});
  }

  getUser(repoId: string, userId: string) {
    const params = {repoId, userId}
    return this.http.get(this.baseUrl + "/get", {params})
  }
}
