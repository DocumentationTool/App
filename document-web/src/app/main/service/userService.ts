import {Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {ApiUser} from '../../api/apiUser';
import {ApiResponseFileTree} from '../../Model/apiResponseFileTree';
import {Repos} from '../../Model/apiResponseModelRepos';
import {User} from '../../Model/apiResponseUser';
import {ToastrService} from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})

export class UserService {
  constructor(
    private router: Router,
    private apiUser: ApiUser,
    private toastr: ToastrService) {
  }

  selectedRepo = signal<Repos | null>(null);
  allUsersOnRepo = signal<User[] | null>(null)

  createUser(repoId: string | undefined, userId: string, password: string, groupIds: string[] | null) {
    this.apiUser.addUser(repoId, userId, password, groupIds).subscribe(
      _ => {
        this.getUser(repoId, null)
        this.toastr.success("User Created")
      },
      error => {
        this.toastr.error(error.message, "User creation failed: ")
      }
    )
  }

  removeUser(repoId: string | undefined, userId: string) {
    this.apiUser.removeUser(repoId, userId).subscribe(
      _ => {
        this.getUser(repoId, null)
        this.toastr.success("User removed")
      },
      error => {
        this.toastr.error(error.message, "User remove failed: ")
      }
    )
  }

  getUser(repoId: string | undefined, UserId: string | null) {
    this.apiUser.getUser(repoId, UserId).subscribe(
      data => {
        this.allUsersOnRepo.set(data.content)
      },
      error => {
        this.toastr.error(error.message, "Load user failed: ")
      }
    )
  }

  addPermissionToUser(repoId: string, userId: string, permissionType: string, path: string) {
    this.apiUser.addPermissionToUser(repoId,userId,permissionType,path).subscribe(
      _ => {
        this.toastr.success("User permission added")
      },
      error => {
        this.toastr.error(error.message, "Add user permission failed: ")
      }
    )
  }

  removePermissionFromUser(repoId: string, userId: string, path: string) {
    this.apiUser.removePermissionFromUser(repoId,userId,path).subscribe(
      _ => {
        this.toastr.success("User permission removed")
      },
      error => {
        this.toastr.error(error.message, "remove user permission failed: ")
      }
    )
  }

  updatePermissionOnUser(repoId: string, userId: string, permissionType: string, path: string) {
    this.apiUser.updatePermissionOnUser(repoId,userId,permissionType,path).subscribe(
      _ => {
        this.toastr.success("User permission updated")
      },
      error => {
        this.toastr.error(error.message, "Update user permission failed: ")

      }
    )
  }
}

