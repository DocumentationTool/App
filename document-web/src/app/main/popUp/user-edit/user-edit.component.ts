import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {User} from '../../../Model/apiResponseUser';
import {ApiGroup} from '../../../api/apiGroup';
import {GroupService} from '../../service/groupService';
import {Permission} from '../../../Model/permission';
import {UserService} from '../../service/userService';
import {ApiUser} from '../../../api/apiUser';
import {ApiResource} from '../../../api/apiResource';
import {Resources} from '../../../Model/apiResponseFileTree';

@Component({
  selector: 'app-user-edit',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './user-edit.component.html',
  styleUrl: './user-edit.component.css'
})
export class UserEditComponent implements OnInit {
  constructor(private dialogRef: MatDialogRef<UserEditComponent>,
              private apiGroup: ApiGroup,
              private apiUser: ApiUser,
              private apiResource: ApiResource,
              private groupService: GroupService,
              private userService: UserService,
              @Inject(MAT_DIALOG_DATA) public dialogData: { repoId: string, user: User }) {
  }

  editUserForm!: FormGroup;

  allGroupsToAdd: string[] = [];
  allGroupsToRemove: string[] = [];
  allPermissions: Permission[] = [Permission.EDIT, Permission.DENY, Permission.ADMIN, Permission.VIEW];
  allPermissionsToRemove: string[] = [];
  allPaths: string[] = [];

  isGroupsToAddActive = false;
  isGroupsToRemoveActive = false;
  isPermissionToAddActive = false;
  isPathActive = false;
  isPermissionToRemoveActive = false;

  ngOnInit() {
    this.editUserForm = new FormGroup({
      groupsToAdd: new FormControl('', []),
      groupsToRemove: new FormControl('', []),
      permissionToAdd: new FormControl('', []),
      pathToAdd: new FormControl('', []),
      permissionToRemove: new FormControl('', [])
    });

    this.editUserForm.valueChanges.subscribe(() => {
      this.isFormEmpty();
    });

    this.apiGroup.getGroup(null).subscribe(
      data => {
        if (data && data.content) {
          this.allGroupsToAdd = data.content.map(group => group.groupId)
        }
      }
    )
    this.apiUser.getUser(this.dialogData.user.userId).subscribe(
      data => {
        if (data && data.content) {
          this.allGroupsToRemove = data.content.flatMap(group => group.groups)
        }
      }
    )
    this.apiUser.getUser(this.dialogData.user.userId).subscribe(
      data => {
        this.allPermissionsToRemove = data.content.flatMap(user =>
          user.permissions.map(permission => permission.path)
        );
      }
    )

    this.apiResource.getResource(null, null, this.dialogData.repoId, this.dialogData.user.userId, [], [], false, 10000).subscribe(
      data => {
        if (data && data.content) {
          this.allPaths = Object.values(data.content).flat()
            .map((resource: Resources) => resource.path);
        } else {
          this.allPaths = [];
        }
      });
  }

  updateUser() {
    const formValue = this.editUserForm.value;

    if (formValue.groupsToAdd) {
      this.groupService.addUserToGroup(this.dialogData.user.userId, formValue.groupsToAdd)
    }

    if (formValue.groupsToRemove) {
      this.groupService.removeUserFromGroup(this.dialogData.user.userId, formValue.groupsToRemove)
    }

    if (formValue.permissionToAdd) {
      this.userService.addPermissionToUser(this.dialogData.repoId, this.dialogData.user.userId, formValue.permissionToAdd, formValue.pathToAdd)
    }

    if (formValue.permissionToRemove) {
      this.userService.removePermissionFromUser(this.dialogData.repoId, this.dialogData.user.userId, formValue.permissionToRemove)
    }
    this.closeDialog();
  }

  onSelectGroupsToAdd(groupId: string) {
    this.editUserForm.controls['groupsToAdd'].setValue(groupId);
    this.isGroupsToRemoveActive = false;
  }

  onSelectGroupsToRemove(groupId: string) {
    this.editUserForm.controls['groupsToRemove'].setValue(groupId);
    this.isGroupsToAddActive = false;
  }

  onSelectPath(path: string) {
    this.editUserForm.controls['pathToAdd'].setValue(path);
    this.isPathActive = false;
  }

  onSelectPermissionToAdd(permission: string) {
    this.editUserForm.controls['permissionToAdd'].setValue(permission);
    this.isPermissionToAddActive = false;
  }

  onSelectPermissionToRemove(permissionPath: string) {
    this.editUserForm.controls['permissionToRemove'].setValue(permissionPath);
    this.isPermissionToRemoveActive = false;
  }

  openGroupsToAddDropdown() {
    this.isGroupsToRemoveActive = false;
    this.isPermissionToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isPathActive = false;
    this.isGroupsToAddActive = true;
  }

  openGroupsToRemoveDropdown() {
    this.isGroupsToAddActive = false;
    this.isPermissionToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isPathActive = false;
    this.isGroupsToRemoveActive = true;
  }

  openPermissionToAddDropdown() {
    this.isGroupsToRemoveActive = false;
    this.isGroupsToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isPathActive = false;
    this.isPermissionToAddActive = true;
  }

  openPathDropdown() {
    this.isGroupsToRemoveActive = false;
    this.isGroupsToAddActive = false;
    this.isPermissionToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isPathActive = true;
  }

  openPermissionToRemoveDropdown() {
    this.isGroupsToRemoveActive = false;
    this.isGroupsToAddActive = false;
    this.isPermissionToAddActive = false;
    this.isPathActive = false;
    this.isPermissionToRemoveActive = true;
  }

  getPermissionName(permission: Permission): string {
    return Permission[permission];
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('input.groupsToAdd')) {
      this.isGroupsToAddActive = false;
    }
    if (!target.closest('input.groupsToRemove')) {
      this.isGroupsToRemoveActive = false;
    }
    if (!target.closest('input.addPermission')) {
      this.isPermissionToAddActive = false;
    }
    if (!target.closest('input.removePermission')) {
      this.isPermissionToRemoveActive = false;
    }
    if (!target.closest('input.addPath')) {
      this.isPathActive = false;
    }
  }

  @HostListener('document:keydown.tab', ['$event'])
  onEscapePress(_: KeyboardEvent) {
    this.isGroupsToAddActive = false;
    this.isPermissionToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isGroupsToRemoveActive = false
    this.isPathActive = false;
  }

  isFormEmpty(): boolean {
    const formValue = this.editUserForm.value;
    return !formValue.groupsToAdd &&
      !formValue.groupsToRemove &&
      !formValue.permissionToAdd &&
      !formValue.pathToAdd &&
      !formValue.permissionToRemove;
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
