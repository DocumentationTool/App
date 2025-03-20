import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {ApiResponseUser} from '../Model/apiResponseUser';
import {ApiResponseGroup} from '../Model/apiResponseGroup';

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
    if (userId) params = params.set('userId', userId);
    return this.http.post(this.baseUrl + "/remove", params);
  }

  addUser(repoId: string, userId: string, password: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('userId', userId)
      .set('password', password)
    return this.http.post(this.baseUrl + "/add", params);
  }

  getUser(repoId: string | undefined, userId: string | null) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (userId) params = params.set('userId', userId);

    return this.http.get<ApiResponseUser>(this.baseUrl + "/get", {params})
  }

  removeUserGroup(repoId: string | undefined, groupId: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (groupId) params = params.set('groupId', groupId);
    return this.http.post(this.baseUrl + "/group/remove", params);
  }

  addUserGroup(repoId: string, groupId: string, groupName: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (groupId) params = params.set('groupId', groupId);
    if (groupName) params = params.set('groupName', groupName);
    return this.http.post(this.baseUrl + "/group/add", params);
  }

  getUserGroup(repoId: string | undefined, userId: string | null) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (userId) params = params.set('userId', userId);

    return this.http.get<ApiResponseGroup>(this.baseUrl + "/group/get", {params})
  }
}
