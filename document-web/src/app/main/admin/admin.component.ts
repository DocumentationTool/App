import {Component, OnInit} from '@angular/core';
import {ApiRepo} from '../../api/apiRepo';
import {Repos} from '../../Model/apiResponseModelRepos';

import {NavigationService} from '../service/navigation.service';
import {UserService} from '../service/userService';
import {User} from '../../Model/apiResponseUser';
import {GroupService} from '../service/groupService';

@Component({
  selector: 'app-admin',
  imports: [],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {

  constructor(public navigationService: NavigationService,
              protected userService: UserService,
              private apiRepo: ApiRepo,
              protected groupService: GroupService) {
  }

  allRepos: Repos[] = []
  dropdownOpen: boolean = true;
  editUserActive: boolean = true;


  ngOnInit() {
    this.apiRepo.getRepos().subscribe(
      data => {
        this.allRepos = data.content;
      }
    )
  }

  selectRepo(repo: Repos) {
    this.navigationService.selectRepo(repo);
    this.toggleRepoDropdown();
    this.userService.getUser(this.userService.selectedRepo()?.id, null)
    this.groupService.getGroup(this.userService.selectedRepo()?.id, null)
  }

  toggleRepoDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  toggleManagement() {
    this.editUserActive = !this.editUserActive;
  }

  removeUser(repoId: string | undefined, userId: string) {
    if (window.confirm("Delete user: " + userId)) {
      this.userService.removeUser(repoId, userId)
    }
  }

  editUser(repoId: string | undefined, user: User
  ) {
    this.navigationService.editUser(repoId, user);
  }

  removeGroup(repoId: string | undefined, groupId: string
  ) {
    if (window.confirm("Delete group: " + groupId)) {
      this.groupService.removeGroup(repoId, groupId)
      this.groupService.getGroup(this.userService.selectedRepo()?.id, null)
    }
  }

  editGroup(repoId: string | undefined, groupId: string
  ) {
    this.navigationService.editGroup(repoId, groupId);
  }

}
