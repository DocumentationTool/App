import {Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {ApiAuth} from '../../api/apiAuth';
import {ApiResource} from '../../api/apiResource';
import {ResourceService} from './resource.service';
import {ApiResponseModelResourceBeingEdited} from '../../Model/apiResponseModelResourceBeingEdited';
import {ResourceCreateNewComponent} from '../popUp/resource-createNew/resource-createNew.component';
import {MatDialog} from '@angular/material/dialog';
import {ResourceUploadComponent} from '../popUp/resource-upload/resource-upload.component';

@Injectable({
  providedIn: 'root'
})

export class NavigationService {
  constructor(private router: Router,
              private apiAuth: ApiAuth,
              private apiResource: ApiResource,
              private resourceService: ResourceService,
              private dialog: MatDialog) {
  }

  toggle = signal<boolean>(true);
  isAdmin = signal<boolean>(false);
  mode = signal<string>("editor");
  editTags = signal<boolean>(false);
  moveResource = signal<boolean>(false);

  logAdminIn() {
    this.isAdmin.set(true);
  }

  logUserIn() {
    this.isAdmin.set(false);
    //ToDo: ausbauen:
    this.getTestUser();
  }

  getTestUser() {
    this.apiAuth.testLogin().subscribe(response => {
        console.log(response)
      },
      (error) => {
        console.error("Fehler Bei testLogin ", error)
      });
  }

  toggleSidebar() {
    this.toggle.update(current => !current);
  }

  onEditor() {
    if (!this.isEditorActive()) { //wenn nur zwischen editor und preview hin und her gesrpungen wird, keine abfrage ob file editiert wird
      this.apiResource.checksResourceBeingEdited(this.resourceService.selectedFile()?.repoId, this.resourceService.selectedFile()?.path).subscribe(
        data => {
          console.log("data ", data)
          this.editedResourceCheck(data)
        },
        error => {
          console.error(error)
        }
      )
    } else {
      this.mode.set("editor")
    }
  }

  editedResourceCheck(data: ApiResponseModelResourceBeingEdited) {
    if (!data.content.isBeingEdited) { //Abfrage ob file editiert wird
      let userId = "Niklas" //ToDo: user id dynamic   -- user anlegen sonst bad request!
      console.log("File Editing")
      // this.apiResource.setResourceBeingEdited(this.resourceService.selectedFile()?.repoId, this.resourceService.selectedFile()?.path, userId).subscribe(
      //   data => {
      //     console.log("data ", data)
      //   },
      //   error => {
      //     console.error(error)
      //   }
      // )
      this.router.navigate(['/main/editor'])
      this.mode.set("editor")
      this.resourceService.editingFile.set(this.resourceService.selectedFile());
    } else {
      console.log("file is being edited")
    }
  }

  onPreview() {
    this.mode.set("preview")
  }

  createNewResource() {
    this.dialog.open(ResourceCreateNewComponent);
  }

  uploadNewResource(data: string, path: string){
    this.dialog.open(ResourceUploadComponent,
      {
        data: {data,path}
      });
  }

  isEditorActive(): boolean {
    return this.router.isActive('/main/editor', {
      paths: 'exact',
      queryParams: 'ignored',
      fragment: 'ignored',
      matrixParams: 'ignored',
    });
  }
}
