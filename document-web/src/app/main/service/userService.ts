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

  createUser(repoId: string, userId: string, password: string) {
    this.apiUser.addUser(repoId, userId, password).subscribe(
      data => {
        console.log(data)
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
        console.log(data)
      },
      error => {
        console.error(error)
      }
    )
  }

}
