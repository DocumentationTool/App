import {Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {ApiAuth} from '../../api/apiAuth';
import {ApiResource} from '../../api/apiResource';
import {ResourceService} from './resource.service';
import {ApiResponseModelResourceBeingEdited} from '../../Model/apiResponseModelResourceBeingEdited';

@Injectable({
  providedIn: 'root'
})

export class NavigationService {
  constructor(private router: Router,
              private apiAuth: ApiAuth,
              private apiResource: ApiResource,
              private resourceService: ResourceService) {
  }

  toggle = signal<boolean>(true);
  isAdmin = signal<boolean>(false);
  mode = signal<string>("editor");

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
    } else {
      console.log("file is being edited")
    }
  }

  onPreview() {
    this.mode.set("preview")
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
