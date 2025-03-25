import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ApiResponseFileTree, Resources} from '../Model/apiResponseFileTree';
import {ApiResponseResource} from '../Model/apiResponseResource';
import {ApiResponseModelResourceBeingEdited} from '../Model/apiResponseModelResourceBeingEdited';
import {ApiResponseTags} from '../Model/apiResponseTags';

@Injectable({
  providedIn: 'root'
})
export class ApiResource {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/resource';

  updateResource(repoId: string | undefined, path: string | undefined, userId: string | null, tagsToAdd: string[] | null, tagsToRemove: string[] | null,
                 tagsToSet: string[] | null, category: string | null, data: string | null, treatNullsAsValues: boolean) {
    const payload = {
      "repoId": repoId,
      "path": path,
      "userId": userId,
      "tagsToAdd":
      tagsToAdd
      ,
      "tagsToRemove":
      tagsToRemove
      ,
      "tagsToSet":
      tagsToSet
      ,
      "category": category,
      "data": data
    }
    return this.http.post(this.baseUrl + "/update", payload)
  }

  addTag(repoId: string, tagId: string, tagName: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('tagId', tagId)
      .set('tagName', tagName)
    return this.http.post(this.baseUrl + "/tag/add", params)
  }

  setResourceBeingEdited(repoId: string | undefined, path: string | undefined, userId: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (path) params = params.set('path', path);
    if (path) params = params.set('userId', userId);
    return this.http.post(this.baseUrl + "/editing/set", params)
  }

  addResource(repoId: string, path: string, createdBy: string, category: string | null, tagIds: string[] | null, data: string) {
    let params = new HttpParams();

    if (repoId) params = params.set('repoId', repoId);
    if (path) params = params.set('path', path);
    if (createdBy) params = params.set('createdBy', createdBy);
    if (category) params = params.set('category', category);
    if (tagIds && tagIds.length > 0) {
      tagIds.forEach(tag => {
        params = params.append('tagIds', tag); // Fügt jedes Tag als separaten Parameter hinzu
      });
    }
    return this.http.post(this.baseUrl + "/add", data, {params});
  }

  removeTag(repoId: string, tagId: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('tagId', tagId)
    return this.http.post(this.baseUrl + "/tag/remove", params)
  }

  getTag(repoId: string | null) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    return this.http.post<ApiResponseTags>(this.baseUrl + "/tag/get", params)
  }

  removeResource(repo: string, path: string) {
    const params = new HttpParams()
      .set('repoId', repo)
      .set('path', path)
    return this.http.post(this.baseUrl + "/remove", params)
  }

  moveResource(userId: string, repoFrom: string, pathFrom: string, repoTo: string, pathTo: string) {
    let params = new HttpParams()
    if (userId) params = params.set('userId', userId);
    if (repoFrom) params = params.set('repoFrom', repoFrom);
    if (pathFrom) params = params.set('pathFrom', pathFrom);
    if (repoTo) params = params.set('repoTo', repoTo);
    if (pathTo) params = params.set('pathTo', pathTo);
    return this.http.post(this.baseUrl + "/move", params)
  }

  getResource(searchTerm: string | null, path: string | null, repoId: string | null, userId: string | null,
              whitelistTags: string[], blacklistTags: string[], withData: boolean, returnLimit: number) {
    const payload = {
      "searchTerm": searchTerm,
      "path": path,
      "repoId": repoId,
      "userId": userId,
      "whitelistTags": whitelistTags,
      "blacklistTags": blacklistTags,
      "withData": withData,
      "returnLimit": returnLimit
    }
    return this.http.post<ApiResponseResource>(this.baseUrl + "/get", payload);
  }

  loadFileTree(searchTerm: string | null, path: string | null, repoId: string | null, userId: string | null,
               whiteListTags: string[], blacklistListTags: string[], withData: boolean, returnLimit: number) {
    const payload = {
      "searchTerm": searchTerm,
      "path": path,
      "repoId": repoId,
      "userId": userId,
      "whiteListTags": whiteListTags,
      "blacklistListTags": blacklistListTags,
      "withData": withData,
      "returnLimit": returnLimit
    }
    return this.http.post<ApiResponseFileTree>(this.baseUrl + "/get/filetree", payload);
  }

  removesResourceBeingEdited(repoId: string | undefined, path: string | undefined) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (path) params = params.set('path', path);
    return this.http.post(this.baseUrl + "/editing/remove", params)
  }

  checksResourceBeingEdited(repoId: string | undefined, path: string | undefined) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (path) params = params.set('path', path);
    return this.http.get<ApiResponseModelResourceBeingEdited>(this.baseUrl + "/editing/get", {params})
  }
}
