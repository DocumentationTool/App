import {Injectable, signal, WritableSignal} from '@angular/core';
import {Router} from '@angular/router';
import {ApiResource} from '../../api/apiResource';
import {ApiResponseFileTree, Resources} from '../../Model/apiResponseFileTree';

@Injectable({
  providedIn: 'root'
})

export class ResourceService {
  constructor(
    private router: Router,
    private apiResource: ApiResource) {
  }

  fileTree = signal<ApiResponseFileTree | null>(null); //Die Repos und Files in einer Struktur
  selectedFile = signal<Resources | null>(null); //Das derzeit ausgewählte File
  private _fileContent = signal<string>("");
  fileContentBeforeChanges = "";

  onSelectResource(file: any) {
    if (this.checkForFileChanges()){
      console.log("änderung")
      if(window.confirm("Save changes?")){
        this.updateResource();
        this.selectResource(file);
      }
    } else {
      console.log("keine änderung")
      this.selectResource(file);
    }
  }

  selectResource(file: any) { //Bei file Auswahl, den Inhalt holen
      this.apiResource.getResource(null, file.path, file.repoId, null, [], [], true, 1).subscribe(
        data => {
          // Greife auf die Ressourcen des entsprechenden Repos zu
          const resources: Resources[] | undefined = data.content[file.repoId];
          if (resources && resources.length > 0) {
            // Optional: Finde den Resource-Eintrag anhand des Pfads, falls es mehrere Einträge gibt
            const resource = resources.find(r => r.path === file.path);
            if (resource) {
              this.selectedFile.set(resource);
              this._fileContent.set(resource.data);
              this.fileContentBeforeChanges = resource.data;
              this.router.navigate(['/main/view'])

            } else {
              console.error("Kein Resource-Eintrag gefunden für den Pfad:", file.path);
            }
          } else {
            console.error("Keine Ressourcen gefunden für repoId:", file.repoId);
          }
        },
        error => {
          console.error(error);
        }
      );
  }

  checkForFileChanges() {
    return this._fileContent() != this.fileContentBeforeChanges;
  }

  get fileContent(): WritableSignal<string> {
    return this._fileContent;
  }

  set fileContent(value: WritableSignal<string>) {
    this._fileContent = value;
  }

  updateResource() {
    this.apiResource.updateResource(this.selectedFile()?.repoId, this.selectedFile()?.path, null, [], [], [], null, this.fileContent(), false).subscribe(
      data => {
        console.log(data)
        this.loadFileTree();
        this.fileContentBeforeChanges = this._fileContent();
      },
      error => {
        console.error(error);
      }
    )
  }

  addTag() {
    this.apiResource.addTag("repo1", "1", "school").subscribe(
      data => {
        this.loadFileTree();
      },
      error => {
        console.error(error);
      }
    )
  }

  addResource() {
    this.apiResource.addResource("repo2", "TestFile1.md", "Niklas.F", "", "# TestFile1 im repo2").subscribe(
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

  moveResource() {

  }

  getResource() {

  }

  loadFileTree() {
    this.apiResource.loadFileTree(null, null, null, null, [], [], false, 1073741824).subscribe(
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
