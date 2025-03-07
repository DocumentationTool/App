import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiPermission {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/permission';

  removeUserPermission(repoId: string, userId: string, permissionType: string, path: string) {
    const params = {repoId, userId, permissionType, path}
    return this.http.get(this.baseUrl + "/user/remove", {params})
  }

  getUserPermission(repoId: string, userId: string) {
    const params = {repoId, userId}
    return this.http.get(this.baseUrl + "/user/get", {params})
  }

  addUserPermission(repoId: string, userId: string, permissionType: string, path: string) {
    const params = {repoId, userId, permissionType, path}
    return this.http.get(this.baseUrl + "/user/add", {params})
  }

  removeGroupPermission(repoId: string, groupId: string, permissionType: string, path: string) {
    const params = {repoId, groupId, permissionType, path}
    return this.http.get(this.baseUrl + "/group/remove", {params})
  }

  getGroupPermission(repoId: string, groupId: string) {
    const params = {repoId, groupId}
    return this.http.get(this.baseUrl + "/group/get", {params})
  }

  addGroupPermission(repoId: string, groupId: string, permissionType: string, path: string) {
    const params = {repoId, groupId, permissionType, path}
    return this.http.get(this.baseUrl + "/group/add", {params})
  }


}

