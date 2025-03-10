import {Injectable, signal, WritableSignal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {ApiResource} from '../../api/apiResource';

@Injectable({
  providedIn: 'root'
})

export class FileService {
  constructor(private http: HttpClient,
              private router: Router,
              private apiResource: ApiResource) {
  }

  files = signal<string[]>([]);
  selectedFile = signal<string | null>(null);
  private _fileContent = signal<string>("");
  fileContentBeforeChanges = "";

  loadFiles() {
    this.apiResource.getFiletree("repo1","").subscribe(
      data => this.files.set(data)
    )
    this.http.get<string[]>('file-list.json')
      .subscribe(data => this.files.set(data));
  }

  setSelectedFile(file: string) {
    this.selectedFile.set(file);
    this.loadFileContent().subscribe(content => {
      this._fileContent.set(content);
      this.fileContentBeforeChanges = content;
    }, error => {
      console.error('Fehler beim Laden der Datei:', error);
    });

    this.router.navigate(['/main/view'])
  }

  loadFileContent() {
    return this.http.get('assets/' + this.selectedFile(), {responseType: 'text'})
  }

  checkForFileChanges() {
    if (this._fileContent() != this.fileContentBeforeChanges) {
      this.saveFile();
    }
  }

  get fileContent(): WritableSignal<string> {
    return this._fileContent;
  }

  set fileContent(value: WritableSignal<string>) {
    this._fileContent = value;
  }

  saveFile() {
    if (window.confirm("Save changes?")) {
      //Todo: save
      this.fileContentBeforeChanges = this._fileContent();

    } else {
      return
    }
  }
}
