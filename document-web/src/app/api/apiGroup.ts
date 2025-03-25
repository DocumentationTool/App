import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {ApiResponseGroup} from '../Model/apiResponseGroup';
import {Permission} from '../Model/permission';

@Injectable({
  providedIn: 'root'
})
export class ApiGroup{
  constructor(private http: HttpClient) {
  }
  private baseUrl = 'http://localhost:8080/api/group';


  addGroup(groupId: string, groupName:string) {
    let params = new HttpParams()
    if (groupId) params = params.set('groupId', groupId);
    if (groupName) params = params.set('groupName', groupName);
    return this.http.post(this.baseUrl + "/add", params);
  }

  removeGroup(groupId: string) {
    let params = new HttpParams()
    if (groupId) params = params.set('groupId', groupId);
    return this.http.post(this.baseUrl + "/remove", params);
  }

  renameGroup(groupId: string, newName:string) {
    let params = new HttpParams()
    if (groupId) params = params.set('groupId', groupId);
    if (newName) params = params.set('newName', newName);
    return this.http.post(this.baseUrl + "/rename", params);
  }

  getGroup(groupId: string | null) {
    let params = new HttpParams()
    if (groupId) params = params.set('groupId', groupId);
    return this.http.get<ApiResponseGroup>(this.baseUrl + "/get", {params})
  }

  addUserToGroup( userId:string, groupId: string) {
    let params = new HttpParams()
    if (userId) params = params.set('userId', userId);
    if (groupId) params = params.set('groupId', groupId);
    return this.http.post(this.baseUrl + "/user/add", params);
  }

  removeUserFromGroup(userId:string, groupId: string) {
    let params = new HttpParams()
    if (userId) params = params.set('userId', userId);
    if (groupId) params = params.set('groupId', groupId);
    return this.http.post(this.baseUrl + "/user/remove", params);
  }

  addPermissionToGroup(repoId: string | undefined, groupId: string, permission: string, path: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (groupId) params = params.set('groupId', groupId);
    if (permission) params = params.set('permission', permission);
    if (path) params = params.set('path', path);
    return this.http.post(this.baseUrl + "/permission/add", params);
  }

  removePermissionFromGroup(repoId: string | undefined, groupId: string, path: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (groupId) params = params.set('groupId', groupId);
    if (path) params = params.set('path', path);
    return this.http.post(this.baseUrl + "/permission/remove", params);
  }

  updatePermissionOnGroup(repoId: string | undefined, groupId: string, permission: Permission, path: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (groupId) params = params.set('groupId', groupId);
    if (permission) params = params.set('permission', permission);
    if (path) params = params.set('path', path);
    return this.http.post(this.baseUrl + "/permission/update", params);
  }




}
