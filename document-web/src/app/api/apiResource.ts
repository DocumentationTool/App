import {HttpClient} from '@angular/common/http';
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
    const params = {repoId, path, createdBy, category}
    return this.http.put(this.baseUrl + "/add", content, {params})
  }

  getResource(repo: string, user: string) {
    const payload = {repo, user};
    return this.http.get(this.baseUrl + "/get");
  }

  getFiletree(repo: string, user: string) {
    const payload = {
      "searchTerm": null,
      "path": null,
      "repoId": "repo1",
      "userId": null,
      "whiteListTags": [

      ],
      "blacklistListTags": [

      ],
      "withData": true,
      "returnLimit": 1073741824
    }
    return this.http.post<ApiResponseFileTree>(this.baseUrl + "/get/filetree", payload);
  }
}
