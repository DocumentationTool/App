import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiRole {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/role';

  getRole(repoId: string, userId: string) {
    const params = {repoId, userId}
    return this.http.get(this.baseUrl + "/get");
  }
}
