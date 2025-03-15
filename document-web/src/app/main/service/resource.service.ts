import {Injectable, signal, WritableSignal} from '@angular/core';
import {Router} from '@angular/router';
import {ApiResource} from '../../api/apiResource';
import {ApiResponseFileTree, Resources} from '../../Model/apiResponseFileTree';
import {ApiResponseResource} from '../../Model/apiResponseResource';

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
  editingFile = signal<Resources | null>(null)
  searchResults = signal<ApiResponseResource | null> (null);
  private _fileContent = signal<string>("");
  fileContentBeforeChanges = "";

  onSelectResource(file: any) {
    if (this.checkForFileChanges()) {
      if (window.confirm("Save changes?")) {
        this.updateResource();
        this.selectResource(file);
      }
    } else {
      this.selectResource(file);
    }
  }

  selectResource(file: any) { //Bei file Auswahl, den Inhalt holen
    this.apiResource.getResource(null, this.splitResourcePath(file.path), file.repoId, null, [], [], true, 1).subscribe(
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

  removeFileEditing() {
    console.log("remove FIle Edit: ", this.editingFile()?.repoId, this.editingFile()?.path)
    this.apiResource.removesResourceBeingEdited(this.editingFile()?.repoId, this.editingFile()?.path)
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
    this.apiResource.updateResource(this.editingFile()?.repoId, this.editingFile()?.path, null, [], [], [], null, this.fileContent(), false).subscribe(
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

  addResource(repoId: string, path: string, createdBy: string, category: string | null, tagIds: string[] | null, data: string) {
    this.apiResource.addResource(repoId, path, createdBy, category, tagIds, data).subscribe(
      data => {
        console.log("data", data)
        this.loadFileTree();
      },
      error => {
        console.error(error.error.error)
      }
    )
  }

  removeTag() {

  }

  getTag(repoId: string | null) {
    this.apiResource.getTag(repoId)
  }

  removeResource(repoId: string, path: string) {
    this.apiResource.removeResource(repoId, path).subscribe(
      data => {
        //ToDo: erfolgsnachricht
        this.loadFileTree();
      },
      error => {
        console.error(error)
      }
    )
  }

  moveResource() {

  }

  getResource(searchTerm: string | null, path: string | null, repoId: string | null, userId: string | null,
              whiteListTags: string[], blacklistListTags: string[]) {
    this.apiResource.getResource(searchTerm, path, repoId, userId, whiteListTags, blacklistListTags, false, 1073741824).subscribe(
      data => {
        this.searchResults.set(data);
      },
      error => {
        console.error(error)
      }
    )
  }

  loadFileTree() {
    this.apiResource.loadFileTree(null, null, null, null, [], [], false, 1073741824).subscribe(
      data => {
        this.fileTree.set(data);
        console.log(this.fileTree());
        console.log(this.fileTree()?.content);
      },
      error => {
        console.error('Error loading file tree:', error);
      }
    );
  }

  get content(){
    return this.fileTree();
  }

  splitResourcePath(path: string) {
    return path.split("\\").pop() || '';
  }
}
