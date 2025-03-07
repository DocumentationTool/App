import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {ApiResponse} from '../Model/DocumentContent';

@Injectable({
  providedIn: 'root'
})
export class ApiDocument {
  private baseUrl = 'http://localhost:8080/api/document/get';

  constructor(private http: HttpClient) {}

  getDocument(repo: string, user: string) {
    const params = { repo, user };
    return this.http.get<ApiResponse>(this.baseUrl, { params });
  }
}
