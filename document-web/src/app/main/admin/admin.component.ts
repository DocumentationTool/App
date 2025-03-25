import {Component, OnInit} from '@angular/core';

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
              protected groupService: GroupService) {
  }
  editUserActive: boolean = true;

  ngOnInit() {
    this.userService.getUser(null)
    this.groupService.getGroup(null)
  }

  toggleManagement() {
    this.editUserActive = !this.editUserActive;
  }

  removeUser(userId: string) {
    if (window.confirm("Delete user: " + userId)) {
      this.userService.removeUser(userId)
    }
  }

  editUser(repoId: string | undefined, user: User
  ) {
    this.navigationService.editUser(repoId, user);
  }

  removeGroup(groupId: string
  ) {
    if (window.confirm("Delete group: " + groupId)) {
      this.groupService.removeGroup(groupId)
      this.groupService.getGroup(null)
    }
  }

  editGroup(groupId: string
  ) {
    this.navigationService.editGroup(groupId);
  }

}
