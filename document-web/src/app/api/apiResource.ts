import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ApiResponseModel} from '../Model/ApiResponseModel';

@Injectable({
  providedIn: 'root'
})
export class ApiResource {
  constructor(private http: HttpClient) {
  }

  private baseUrl = 'http://localhost:8080/api/resource';

  updateResource(repoId: string, path: string, createdBy: string, category: string) {
    const params = {repoId, path, createdBy, category}
    return this.http.put(this.baseUrl + "/update", {params})
  }

  addResource(repoId: string, path: string, createdBy: string, category: string) {
    const params = {repoId, path, createdBy, category}
    return this.http.put(this.baseUrl + "/add", {params})
  }

  getResource(repo: string, user: string) {
    const params = {repo, user};
    return this.http.get(this.baseUrl + "/get");
  }

  getFiletree(repo: string, user: string) {
    const params = {repo, user};
    return this.http.get(this.baseUrl + "/get/filetree");
  }
}
