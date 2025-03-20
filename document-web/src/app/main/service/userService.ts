import {Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {ApiUser} from '../../api/apiUser';
import {ApiResponseFileTree} from '../../Model/apiResponseFileTree';
import {Repos} from '../../Model/apiResponseModelRepos';
import {User} from '../../Model/apiResponseUser';

@Injectable({
  providedIn: 'root'
})

export class UserService {
  constructor(
    private router: Router,
    private apiUser: ApiUser) {
  }

  selectedRepo = signal<Repos | null>(null);
  allUsersOnRepo = signal<User[] | null>(null)
  allGroupsOnUser = signal<string[] | null>(null)

  createUser(repoId: string, userId: string, password: string) {
    this.apiUser.addUser(repoId, userId, password).subscribe(
      data => {
        console.log(data)
        this.getUser(repoId, null)
      },
      error => {
        console.error(error)
      }
    )
  }

  getUser(repoId: string | undefined, UserId: string | null) {
    console.log(repoId)
    this.apiUser.getUser(repoId, UserId).subscribe(
      data => {
        this.allUsersOnRepo.set(data.content)
      },
      error => {
        console.error(error)
      }
    )
  }

  removeUser(repoId: string | undefined, userId: string) {
    this.apiUser.removeUser(repoId, userId).subscribe(
      data => {
        this.getUser(repoId, null)
      },
      error => {
        console.error(error)
      }
    )
  }

  editGroups(repoId: string, tagIdsToAdd: string[], tagNamesToAdd: string[], tagIdsToRemove: string[]) {
    if (tagIdsToAdd && tagNamesToAdd && tagIdsToAdd.length === tagNamesToAdd.length) {
      for (let i = 0; i < tagIdsToAdd.length; i++) {
        this.apiUser.addUserGroup(repoId, tagIdsToAdd[i], tagNamesToAdd[i]).subscribe(
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
        this.apiUser.removeUserGroup(repoId, tagIdsToRemove[i]).subscribe(
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

  removeUserGroup(repoId: string | undefined, groupId: string) {
    this.apiUser.removeUserGroup(repoId, groupId).subscribe(
      data => {
        console.log(data)
      },
      error => {
        console.error(error)
      }
    )
  }

  getUserGroup(repoId: string | undefined, userId: string | null) {
    this.apiUser.getUserGroup(repoId, userId).subscribe(
      data => {
        if (data.content && data.content.length > 0) {
          // Extrahiere alle groupId-Werte aus der Antwort
          const allGroupIds: string[] = data.content.map(group => group.groupId);

          this.allGroupsOnUser.set(allGroupIds);
        } else {
          console.log("No Groups on User");
          this.allGroupsOnUser.set([]);
        }
      },
      error => {
        console.error("Error fetching user groups:", error);
      }
    );
  }
}

