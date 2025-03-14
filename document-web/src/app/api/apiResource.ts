import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ApiResponseFileTree, Resources} from '../Model/apiResponseFileTree';
import {ApiResponseResource} from '../Model/apiResponseResource';
import {ApiResponseModelResourceBeingEdited} from '../Model/apiResponseModelResourceBeingEdited';

@Injectable({
  providedIn: 'root'
})
export class ApiResource {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/resource';

  updateResource(repoId: string | undefined, path: string | undefined, userId: string | null, tagsToAdd: string[], tagsToRemove: string[],
                 tagsToSet: string[], category: string | null, data: string, treatNullsAsValues: boolean) {
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
    return this.http.put(this.baseUrl + "/update", payload)
  }

  addTag(repoId: string, tagId: string, tagName: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('tagId', tagId)
      .set('tagName', tagName)
    return this.http.put(this.baseUrl + "/tag/add", params)
  }

  setResourceBeingEdited(repoId: string | undefined, path: string | undefined, userId: string) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (path) params = params.set('path', path);
    if (path) params = params.set('userId', userId);
    return this.http.put(this.baseUrl + "/editing/set", params)
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
    return this.http.put(this.baseUrl + "/add", data, {params});
  }

  removeTag(userId: string, repoFrom: string, from: string, repoTo: string, to: string) {
    const params = new HttpParams()
      .set('repoFrom', repoFrom)
      .set('from', from)
      .set('repoTo', repoTo)
      .set('to', to);
    return this.http.post(this.baseUrl + "/tag/remove", {params: params})
  }

  getTag(userId: string, repoFrom: string, from: string, repoTo: string, to: string) {
    const params = new HttpParams()
      .set('repoFrom', repoFrom)
      .set('from', from)
      .set('repoTo', repoTo)
      .set('to', to);
    return this.http.post(this.baseUrl + "/tag/get", {params: params})

  }

  removeResource(repo: string, path: string) {
    const params = new HttpParams()
      .set('repoId', repo)
      .set('path', path)
    return this.http.post(this.baseUrl + "/remove", params)
  }

  moveResource(userId: string, repoFrom: string, pathFrom:string, repoTo: string, pathTo:string) {
    const params = new HttpParams()
      .set('userId', userId)
      .set('repoFrom', repoFrom)
      .set('pathFrom', pathFrom)
      .set('repoTo', repoTo)
      .set('pathTo', pathTo)
    return this.http.post(this.baseUrl + "/move", params)

  }

  getResource(searchTerm: string | null, path: string | null, repoId: string | null, userId: string | null,
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
    return this.http.post(this.baseUrl + "/editing/remove", {params})
  }

  checksResourceBeingEdited(repoId: string | undefined, path: string | undefined) {
    let params = new HttpParams()
    if (repoId) params = params.set('repoId', repoId);
    if (path) params = params.set('path', path);
    return this.http.get<ApiResponseModelResourceBeingEdited>(this.baseUrl + "/editing/get", {params})
  }
}
