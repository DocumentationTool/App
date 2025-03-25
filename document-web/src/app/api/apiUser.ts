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

  addUser(repoId: string | undefined, userId: string, password: string,role: string, groupIds: string[] | null) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId)
      .set('userId', userId)
      .set('password', password)
      .set('roles', role)
    if (groupIds && groupIds.length > 0) {
      groupIds.forEach(groupId => {
        params = params.append('tagIds', groupId); // Fügt jede groupId als separaten Parameter hinzu
      });
    }
    return this.http.post(this.baseUrl + "/add", params);
  }

  removeUser(repoId: string | undefined, userId: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (userId) params = params.set('userId', userId);
    return this.http.post(this.baseUrl + "/remove", params);
  }

  getUser(repoId: string | undefined, userId: string | null) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (userId) params = params.set('userId', userId);

    return this.http.get<ApiResponseUser>(this.baseUrl + "/get", {params})
  }

  addPermissionToUser(repoId: string, userId: string, permissionType: string, path: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('userId', userId)
      .set('permission', permissionType)
      .set('path', path)
    return this.http.post(this.baseUrl + "/permission/add", params)
  }

  removePermissionFromUser(repoId: string, userId: string, path: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('userId', userId)
      .set('path', path)
    return this.http.post(this.baseUrl + "/permission/remove", params)
  }

  updatePermissionOnUser(repoId: string, userId: string, permissionType: string, path: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('userId', userId)
      .set('permissionType', permissionType)
      .set('path', path)
    return this.http.post(this.baseUrl + "/permission/update", params)
  }

}
