import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {ApiResponseUser} from '../Model/apiResponseUser';

@Injectable({
  providedIn: 'root'
})
export class ApiUser {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/user';

  removeUser(repoId: string | undefined, userId: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (userId) params = params.set('path', userId);
    return this.http.put(this.baseUrl + "/remove", params);
  }

  addUser(repoId: string, userId: string, password: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('userId', userId)
      .set('password', password)
    return this.http.put(this.baseUrl + "/add", params);
  }

  getUser(repoId: string | undefined, userId: string | null) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (userId) params = params.set('userId', userId);

    return this.http.get<ApiResponseUser>(this.baseUrl + "/get", {params})
  }

  removeUserGroup(repoId: string | undefined, userId: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (userId) params = params.set('path', userId);
    return this.http.put(this.baseUrl + "/group/remove", params);
  }

  addUserGroup(repoId: string, userId: string, password: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('userId', userId)
      .set('password', password)
    return this.http.put(this.baseUrl + "/group/add", params);
  }

  getUserGroup(repoId: string | undefined, userId: string | null) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (userId) params = params.set('userId', userId);

    return this.http.get<ApiResponseUser>(this.baseUrl + "/group/get", {params})
  }
}
