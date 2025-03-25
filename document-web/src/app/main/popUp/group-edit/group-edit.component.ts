import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {GroupService} from '../../service/groupService';
import {FormsModule} from '@angular/forms';
import {ApiUser} from '../../../api/apiUser';
import {ApiGroup} from '../../../api/apiGroup';
import {Permission} from '../../../Model/permission';
import {Resources} from '../../../Model/apiResponseFileTree';
import {ApiResource} from '../../../api/apiResource';

@Component({
  selector: 'app-group-edit',
  imports: [
    FormsModule
  ],
  templateUrl: './group-edit.component.html',
  styleUrl: './group-edit.component.css'
})
export class GroupEditComponent implements OnInit {
  constructor(private dialogRef: MatDialogRef<GroupEditComponent>,
              private groupService: GroupService,
              private apiUser: ApiUser,
              private apiGroup: ApiGroup,
              private apiResource: ApiResource,
              @Inject(MAT_DIALOG_DATA) public dialogData: {repoId: string, groupId: string }) {
  }

  allUsersToAdd: string[] = [];
  allUsersToRemove: string[] = [];
  allPermissions: Permission[] = [Permission.EDIT, Permission.DENY, Permission.ADMIN, Permission.VIEW];
  allPermissionsToRemove: string[] = [];
  allPaths: string[] = [];
  userToAdd: string = "";
  userToRemove: string = "";
  permissionToAdd: string = "";
  permissionToRemove: string = "";
  pathToAdd: string = "";

  isUsersToAddActive = false;
  isUsersToRemoveActive = false;
  isPermissionToAddActive = false;
  isPermissionToRemoveActive = false;
  isPathActive = false;

  ngOnInit() {
    this.apiUser.getUser(null).subscribe(
      data => {
        if (data && data.content) {
          this.allUsersToAdd = data.content.map(user => user.userId)
        }
      }
    )
    this.apiGroup.getGroup(this.dialogData.groupId).subscribe(
      data => {
        if (data && data.content) {
          this.allUsersToRemove = data.content.flatMap(group => group.users);
        }
      }
    )
    this.apiGroup.getGroup(this.dialogData.groupId).subscribe(
      data => {
        this.allPermissionsToRemove = data.content.flatMap(user =>
          user.permissions.map(permission => permission.path)
        );
      }
    )
    this.apiResource.getResource(null, null, null, null, [], [], false, 10000).subscribe(
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
    if (this.userToAdd) {
      this.groupService.addUserToGroup(this.userToAdd, this.dialogData.groupId)
    }

    if (this.userToRemove) {
      this.groupService.removeUserFromGroup(this.userToRemove, this.dialogData.groupId)
    }

    if (this.permissionToAdd) {
      this.groupService.addPermissionToGroup(this.dialogData.repoId, this.dialogData.groupId, this.permissionToAdd, this.pathToAdd)
    }

    if (this.permissionToRemove) {
      this.groupService.removePermissionFromGroup(this.dialogData.repoId, this.dialogData.groupId, this.permissionToRemove)
    }
    this.closeDialog();
  }

  onSelectUsersToAdd(userId: string) {
    this.userToAdd = userId;
    this.isUsersToRemoveActive = false;
  }

  onSelectUsersToRemove(userId: string) {
    this.userToRemove = userId;
    this.isUsersToAddActive = false;
  }

  onSelectPermissionToAdd(permission: string) {
    this.permissionToAdd = permission;
    this.isPermissionToAddActive = false;
  }

  onSelectPermissionToRemove(permissionPath: string) {
    this.permissionToRemove = permissionPath;
    this.isPermissionToRemoveActive = false;
  }

  onSelectPath(path: string) {
    this.pathToAdd = path;
    this.isPathActive = false;
  }

  openUsersToAddDropdown() {
    this.isUsersToRemoveActive = false;
    this.isPermissionToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isPathActive = false;
    this.isUsersToAddActive = true;
  }

  openUsersToRemoveDropdown() {
    this.isUsersToAddActive = false;
    this.isPermissionToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isPathActive = false;
    this.isUsersToRemoveActive = true;
  }

  openPermissionToAddDropdown() {
    this.isUsersToRemoveActive = false;
    this.isUsersToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isPathActive = false;
    this.isPermissionToAddActive = true;
  }

  openPermissionToRemoveDropdown() {
    this.isUsersToRemoveActive = false;
    this.isUsersToAddActive = false;
    this.isPermissionToAddActive = false;
    this.isPathActive = false;
    this.isPermissionToRemoveActive = true;
  }

  openPathDropdown() {
    this.isUsersToRemoveActive = false;
    this.isUsersToAddActive = false;
    this.isPermissionToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isPathActive = true;
  }

  getPermissionName(permission: Permission): string {
    return Permission[permission];
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('input.usersToAdd')) {
      this.isUsersToAddActive = false;
    }
    if (!target.closest('input.usersToRemove')) {
      this.isUsersToRemoveActive = false;
    }
    if (!target.closest('input.addPermission')) {
      this.isPermissionToAddActive = false;
    }
    if (!target.closest('input.removePermission')) {
      this.isPermissionToRemoveActive = false;
    }
    if (!target.closest('input.addPath')) {
      this.isPermissionToRemoveActive = false;
    }
  }

  @HostListener('document:keydown.tab', ['$event'])
  onEscapePress(event: KeyboardEvent) {
    this.isUsersToAddActive = false;
    this.isPermissionToAddActive = false;
    this.isPermissionToRemoveActive = false;
    this.isUsersToRemoveActive = false
    this.isPathActive = false;
  }

  closeDialog() {
    this.dialogRef.close();
  }

}
