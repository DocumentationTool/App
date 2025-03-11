import {Injectable, Resource, signal, WritableSignal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {ApiResource} from '../../api/apiResource';
import {ApiResponseFileTree, Resources} from '../../Model/apiResponseFileTree';

@Injectable({
  providedIn: 'root'
})

export class FileService {
  constructor(private http: HttpClient,
              private router: Router,
              private apiResource: ApiResource) {
  }

  fileTree = signal<ApiResponseFileTree | null>(null);
  selectedFile = signal<Resources | null>(null);
  private _fileContent = signal<string>("");
  fileContentBeforeChanges = "";

  loadFileTree() {
    this.apiResource.getFiletree(null, null, "repo1", null, [], [], true, 1073741824).subscribe(
      data => {
        this.fileTree.set(data);
        console.log(this.fileTree());
      },
      error => {
        console.error('Error loading file tree:', error);
      }
    );
  }

  setSelectedFile(file: any) {
    console.log(file.repoId)
    console.log(file.path)
    console.log(file.data)
    this.selectedFile.set(file);
    this._fileContent.set(file.data);
    this.fileContentBeforeChanges = file.data;

    this.router.navigate(['/main/view'])
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

  addResource() {
    console.log("Upload File")
    this.apiResource.addResource("repo1","TestFile1","Niklas", "", "# TestFile1 im repo1")
  }
}
