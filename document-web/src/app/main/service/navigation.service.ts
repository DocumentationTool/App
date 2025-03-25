import {Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {ApiAuth} from '../../api/apiAuth';
import {ApiResource} from '../../api/apiResource';
import {ResourceService} from './resource.service';
import {ApiResponseModelResourceBeingEdited} from '../../Model/apiResponseModelResourceBeingEdited';
import {ResourceCreateNewComponent} from '../popUp/resource-createNew/resource-createNew.component';
import {MatDialog} from '@angular/material/dialog';
import {ResourceUploadComponent} from '../popUp/resource-upload/resource-upload.component';
import {ResourceEditTagsComponent} from '../popUp/resource-edit-tags/resource-edit-tags.component';
import {ResourceMoveComponent} from '../popUp/resource-move/resource-move.component';
import {RepoEditTagsComponent} from '../popUp/repo-edit-tags/repo-edit-tags.component';
import {UserAddComponent} from '../popUp/user-add/user-add.component';
import {Repos} from '../../Model/apiResponseModelRepos';
import {UserService} from './userService';
import {User} from '../../Model/apiResponseUser';
import {UserEditComponent} from '../popUp/user-edit/user-edit.component';
import {GroupAddComponent} from '../popUp/group-add/group-add.component';
import {GroupEditComponent} from '../popUp/group-edit/group-edit.component';
import {ToastrService} from 'ngx-toastr';
import {AuthService} from './authService';

@Injectable({
  providedIn: 'root'
})

export class NavigationService {
  constructor(private router: Router,
              private apiResource: ApiResource,
              private resourceService: ResourceService,
              private userService: UserService,
              private dialog: MatDialog,
              private authService: AuthService,
              private toastr: ToastrService) {
  }

  toggle = signal<boolean>(true);
  mode = signal<string>("editor");

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
      console.log("File Editing")
      this.apiResource.setResourceBeingEdited(this.resourceService.selectedFile()?.repoId, this.resourceService.selectedFile()?.path, this.authService.username()).subscribe(
        data => {
          console.log("data ", data)
        },
        error => {
          console.error(error.error.error)
        }
      )
      this.router.navigate(['/main/editor'])
      this.mode.set("editor")
      this.resourceService.editingFile.set(this.resourceService.selectedFile());
    } else {
      this.toastr.error("File is being edited")
    }
  }

  onPreview() {
    this.mode.set("preview")
  }

  createNewResource() {
    this.dialog.open(ResourceCreateNewComponent);
  }

  selectRepo(repo: Repos) {
    this.userService.selectedRepo.set(repo)
  }

  createNewUser() {
    this.dialog.open(UserAddComponent);
  }

  createNerGroup() {
    this.dialog.open(GroupAddComponent);
  }

  editGroup(groupId: string) {
    this.dialog.open(GroupEditComponent,
      {
        data: {groupId}
      }
    )
  }

  editUser(repoId: string | undefined, user: User) {
    this.dialog.open(UserEditComponent,
      {
        data: {repoId, user}
      });
  }

  uploadNewResource(data: string, path: string) {
    this.dialog.open(ResourceUploadComponent,
      {
        data: {data, path}
      });
  }

  editResourceTags(repoId: string, path: string) {
    this.dialog.open(ResourceEditTagsComponent,
      {
        data: {repoId, path}
      });
  }

  editRepoTags(repoId: string) {
    this.dialog.open(RepoEditTagsComponent,
      {
        data: {repoId}
      });
  }

  editUnterRepoTags(repoId: string, path: string) {
    this.dialog.open(ResourceEditTagsComponent,
      {
        data: {repoId, path}
      });
  }

  moveResource(repoId: string, path: string) {
    this.dialog.open(ResourceMoveComponent,
      {
        data: {repoId, path}
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

  isAdminActive(): boolean {
    return this.router.isActive('/main/admin', {
      paths: 'exact',
      queryParams: 'ignored',
      fragment: 'ignored',
      matrixParams: 'ignored',
    });
  }
}
