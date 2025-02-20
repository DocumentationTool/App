import {Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})

export class FileService {
  constructor(private http: HttpClient) {
  }
  files = signal<string[]>([]);
  selectedFile = signal<string | null>(null);

  loadFiles() {
    this.http.get<string[]>('file-list.json')
      .subscribe(data => this.files.set(data));
  }

  setSelectedFile(file: string) {
    this.selectedFile.set(file);
  }
}
