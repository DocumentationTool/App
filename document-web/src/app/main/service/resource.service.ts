import {Injectable, Resource, signal, WritableSignal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {ApiResource} from '../../api/apiResource';
import {ApiResponseFileTree, Resources} from '../../Model/apiResponseFileTree';
import {dateTimestampProvider} from 'rxjs/internal/scheduler/dateTimestampProvider';

@Injectable({
  providedIn: 'root'
})

export class ResourceService {
  constructor(private http: HttpClient,
              private router: Router,
              private apiResource: ApiResource) {
  }

  fileTree = signal<ApiResponseFileTree | null>(null); //Die Repos und Files in einer Struktur
  selectedFile = signal<Resources | null>(null); //Das derzeit ausgewählte File
  private _fileContent = signal<string>("");
  fileContentBeforeChanges = "";


  setSelectedFile(file: any) {
    this.selectedFile.set(file);
    this._fileContent.set(file.data);
    this.fileContentBeforeChanges = file.data;
    console.log(this._fileContent())

    this.router.navigate(['/main/view'])
  }

  checkForFileChanges() {
    if (this._fileContent() != this.fileContentBeforeChanges) {
      this.updateResource();
    }
  }

  get fileContent(): WritableSignal<string> {
    return this._fileContent;
  }

  set fileContent(value: WritableSignal<string>) {
    this._fileContent = value;
  }

  updateResource() {
    if (window.confirm("Save changes?")) {
      this.apiResource.updateResource("repo1", "testFile.md", null, [], [], [], null, "# Updated", false).subscribe(
        data => {
          console.log(data)
          this.loadFileTree();
          this.fileContentBeforeChanges = this._fileContent();
        },
        error => {
          console.error(error);
        }
      )
    } else {
      //ToDo: Wenn auf abbrechen beim speichern
      return;
    }
  }

  addTag() {
    this.apiResource.addTag("repo1","1","school").subscribe(
      data => {
        this.loadFileTree();
      },
      error => {
        console.error(error);
      }
    )
  }

  addResource() {
    this.apiResource.addResource("repo2","TestFile1.md","Niklas.F", "", "# TestFile1 im repo2").subscribe(
      data => {
        this.loadFileTree();
      },
      error => {
        console.error(error.error.error)
      }
    )
  }

  removeTag() {

  }

  getTag() {

  }

  removeResource() {

  }

  moveResource(){

  }

  getResource() {

  }

  loadFileTree() {
    this.apiResource.loadFileTree(null, null, null, null, [], [], true, 1073741824).subscribe(
      data => {
        this.fileTree.set(data);
        console.log(this.fileTree());
      },
      error => {
        console.error('Error loading file tree:', error);
      }
    );
  }
}
