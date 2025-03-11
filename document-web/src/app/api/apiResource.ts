import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ApiResponseFileTree} from '../Model/apiResponseFileTree';

@Injectable({
  providedIn: 'root'
})
export class ApiResource {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/resource';

  updateResource(repoId: string, path: string, createdBy: string, category: string) {
    const payload = {repoId, path, createdBy, category}
    return this.http.put(this.baseUrl + "/update", {payload})
  }

  addResource(repoId: string, path: string, createdBy: string, category: string, content: string) {
    const params = new HttpParams()
      .set('repoId', repoId)
      .set('path', path)
      .set('createdBy', createdBy)
      .set('category', category);
    return this.http.put(this.baseUrl + "/add", content, {params})
  }

  removeResource(repo:string, path: string){
    const params = new HttpParams()
      .set('repo', repo)
      .set('path', path)
    return this.http.post(this.baseUrl + "/remove", {params})

  }

  moveResource() {
    const params =""
    return this.http.post(this.baseUrl + "/move", {params})

  }

  getResource(repo: string, user: string) {
    const payload = {repo, user};
    return this.http.get(this.baseUrl + "/get");
  }

  getFiletree(searchTerm: string | null, path: string | null, repoId: string | null, userId: string | null,
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
}
