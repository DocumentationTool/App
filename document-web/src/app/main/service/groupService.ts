import {Injectable, signal} from '@angular/core';
import {ApiGroup} from '../../api/apiGroup';
import {ToastrService} from 'ngx-toastr';
import {Permission} from '../../Model/permission';

@Injectable({
  providedIn: 'root'
})

export class GroupService {
  constructor(private apiGroup: ApiGroup,
              private toastr: ToastrService) {
  }

  allGroupsOnRepo = signal<string[] | null>(null)

  addGroup(repoId: string | undefined, groupId: string, groupName: string) {
    this.apiGroup.addGroup(repoId, groupId, groupName).subscribe(
      _ => {
        this.toastr.success('Group added')
      },
      error => {
        this.toastr.error(error.error.error, "Add group failed: ")
      }
    )
    setTimeout(() => {
      this.getGroup(repoId, null)
    }, 2000);
  }

  removeGroup(repoId: string | undefined, groupId: string) {
    this.apiGroup.removeGroup(repoId, groupId).subscribe(
      _ => {
        this.toastr.success('Group removed')
      },
      error => {
        this.toastr.error(error.error.error, "Group remove failed: ")
      }
    )
    setTimeout(() => {
      this.getGroup(repoId, null)
    }, 2000);
  }

  renameGroup(repoId: string | undefined, groupId: string, newName: string) {
    this.apiGroup.renameGroup(repoId, groupId, newName).subscribe(
      _ => {
        this.toastr.success('Group renamed')
      },
      error => {
        this.toastr.error(error.error.error, "Rename group failed: ")
      }
    )
  }

  getGroup(repoId: string | undefined, groupId: string | null) {
    this.apiGroup.getGroup(repoId, groupId).subscribe(
      data => {
        if (data.content && data.content.length > 0) {
          const allGroupIds: string[] = data.content.map(group => group.groupId);
          this.allGroupsOnRepo.set(allGroupIds);
        } else {
          console.log("No Groups on User");
          this.allGroupsOnRepo.set([]);
        }
      },
      error => {
        this.toastr.error(error.error.error, "Failed get group: ")
      }
    )
  }

  addUserToGroup(repoId: string | undefined, userId: string, groupId: string) {
    this.apiGroup.addUserToGroup(repoId, userId, groupId).subscribe(
      _ => {
        this.toastr.success('User added to group')
      },
      error => {
        this.toastr.error(error.error.error, "Add user to group failed: ")
      }
    )
  }

  removeUserFromGroup(repoId: string | undefined, userId: string, groupId: string) {
    this.apiGroup.removeUserFromGroup(repoId, userId, groupId).subscribe(
      _ => {
        this.toastr.success('User removed from group')
      },
      error => {
        this.toastr.error(error.error.error, "Remove user from group failed: ")
      }
    )
  }

  addPermissionToGroup(repoId: string | undefined, groupId: string, permission: string, path: string) {
    this.apiGroup.addPermissionToGroup(repoId, groupId, permission, path).subscribe(
      _ => {
        this.toastr.success('Permission added to group')
      },
      error => {
        this.toastr.error(error.error.error, "Add permission to group failed: ")
      }
    )
  }

  removePermissionFromGroup(repoId: string | undefined, groupId: string, path: string) {
    this.apiGroup.removePermissionFromGroup(repoId, groupId, path).subscribe(
      _ => {
        this.toastr.success('Permission removed from group')
      },
      error => {
        this.toastr.error(error.error.error, "Remove permission from group failed: ")
      }
    )
  }

  updatePermissionOnGroup(repoId: string | undefined, groupId: string, permission: Permission, path: string) {
    this.apiGroup.updatePermissionOnGroup(repoId, groupId, permission, path).subscribe(
      _ => {
        this.toastr.success('Permission updated on group')
      },
      error => {
        this.toastr.error(error.error.error, "Update permission on group failed: ")
      }
    )
  }
}
