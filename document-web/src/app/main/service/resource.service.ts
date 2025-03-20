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
  searchResults = signal<ApiResponseResource | null>(null);
  private _fileContent = signal<string>("");
  fileContentBeforeChanges = "";
  allTags: string[] | undefined;
  allRepoTagIds = signal<string[]>([]);
  allResourceTagIds = signal<string[]>([]);


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
    this.apiResource.updateResource(this.editingFile()?.repoId, this.editingFile()?.path, null, null, null, null, null, this.fileContent(), false).subscribe(
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

  addResource(repoId: string, path: string, createdBy: string, category: string | null, tagIds: string[] | null, data: string) {
    this.apiResource.addResource(repoId, path, createdBy, category, tagIds, data).subscribe(
      data => {
        this.loadFileTree();
      },
      error => {
        console.error(error.error.error)
      }
    )
  }

  editResourceTags(repoId: string, path: string, tagsToAdd: string[], tagsToRemove: string[]) {

    this.apiResource.updateResource(repoId, path, null, tagsToAdd, tagsToRemove, null, null, null, false).subscribe(
      data => {
        this.loadFileTree();
      },
      error => {
        console.error(error);
      }
    )
  }

  editRepoTags(repoId: string, tagIdsToAdd: string[], tagNamesToAdd: string[], tagIdsToRemove: string[]) {
    if (tagIdsToAdd && tagNamesToAdd && tagIdsToAdd.length === tagNamesToAdd.length) {
      for (let i = 0; i < tagIdsToAdd.length; i++) {
        this.apiResource.addTag(repoId, tagIdsToAdd[i], tagNamesToAdd[i]).subscribe(
          data => {
            console.log(data)
          },
          error => {
            console.error(error)
          }
        );
      }
    }

    if (tagIdsToRemove) {
      for (let i = 0; i < tagIdsToRemove.length; i++) {
        this.apiResource.removeTag(repoId, tagIdsToRemove[i]).subscribe(
          data => {
            console.log(data)
          },
          error => {
            console.error(error)
          }
        );
      }
    }
  }

  getTag(repoId: string) {
    this.apiResource.getTag(repoId).subscribe(
      data => {
        this.allTags = [];
        if (data.content) {
          this.allRepoTagIds.set(Object.entries(data.content).map(([id]) => id));
        }
      },
      error => {
        console.error(error);
      }
    );
  }

  getAllTags() {
    this.apiResource.getTag(null).subscribe(
      data => {
        this.allTags = Object.entries(data.content).map(([id]) => id);
      },
      error => {
        console.error(error)
      }
    )
    return this.allTags;
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

  moveResource(userId: string, repoFrom: string, pathFrom: string, repoTo: string, pathTo: string) {
    this.apiResource.moveResource(userId, repoFrom, pathFrom, repoTo, pathTo).subscribe(
      data => {

      },
      error => {
        console.error(error)
      }
    )
  }

  getResource(searchTerm: string | null, path: string | null, repoId: string | null, userId: string | null,
              whiteListTags: string[], blacklistListTags: string[]) {
    this.apiResource.getResource(searchTerm, path, repoId, userId, whiteListTags, blacklistListTags, false, 1073741824).subscribe(
      data => {
        console.log(data)
        this.searchResults.set(data);
      },
      error => {
        console.error(error)
      }
    )
  }
  // holt alle Tags, von einer Resource
  getResourceTags(searchTerm: string | null, path: string | null, repoId: string | null, userId: string | null,
                  whiteListTags: string[], blacklistListTags: string[]) {
    this.apiResource.getResource(searchTerm, path, repoId, userId, whiteListTags, blacklistListTags, false, 1073741824).subscribe(
      data => { // Alle Tags einer Resource in signal speichern
        let tagsFound = false; // Flag, um zu prüfen, ob Tags vorhanden sind
        Object.values(data.content).forEach((resourcesArray) => {
          resourcesArray.forEach((resource) => {
            if (resource.tags && resource.tags.length > 0) { // Überprüfen, ob Tags vorhanden sind
              tagsFound = true; // Tags gefunden
              resource.tags.forEach((tagId) => { // Direkt durch das tags-Array iterieren
                this.allResourceTagIds.update((tags) => {
                  if (!tags.includes(tagId)) {
                    return [...tags, tagId]; // Neues Array mit dem neuen Tag zurückgeben
                  }
                  return tags;
                });
              });
            }
          });
        });

        // Wenn keine Tags gefunden wurden, allResourceTagIds auf [] setzen
        if (!tagsFound) {
          this.allResourceTagIds.set([]);
        }
      },
      error => {
        console.error(error);
      }
    );
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

  get content() {
    return this.fileTree();
  }

  splitResourcePath(path: string) {
    return path.split("\\").pop() || '';
  }
}
