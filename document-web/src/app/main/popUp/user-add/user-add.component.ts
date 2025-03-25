import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ApiRepo} from '../../../api/apiRepo';
import {UserService} from '../../service/userService';
import {GroupService} from '../../service/groupService';
import {ApiGroup} from '../../../api/apiGroup';
import {group} from '@angular/animations';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-user-add',
  imports: [
    FormsModule
  ],
  templateUrl: './user-add.component.html',
  styleUrl: './user-add.component.css'
})
export class UserAddComponent implements OnInit {
  constructor(private dialogRef: MatDialogRef<UserAddComponent>,
              private userService: UserService,
              private apiRepo: ApiRepo,
              private apiGroup: ApiGroup,
              private toastr: ToastrService) {
  }

  repoId: string | undefined = "";
  userId: string = "";
  password: string = "";
  role: string = "";
  groupIds: string = "";
  repeatPassword: string = "";
  allRepos: string[] = [];
  allRoles: string[] = ["ADMIN", "USER"];
  allGroups: string[] = [];

  isReposActive = false;
  isRoleActive = false;
  isGroupsActive = false;

  ngOnInit() {
    this.apiRepo.getRepos().subscribe(
      data => {
        if (data && data.content) {
          this.allRepos = data.content.map(repo => repo.id);
        }
      }
    )
    this.apiGroup.getGroup(null).subscribe(
      data => {
        if (data && data.content) {
          this.allGroups = data.content.map(group => group.groupId)
        }
      }
    )
  }

  createNewUser() {
    if (this.password === this.repeatPassword) {
      this.userService.createUser(this.userId, this.password,this.role, this.splitGroups(this.groupIds))
      this.closeDialog();
    } else {
      this.toastr.error("password dont match")
    }
  }

  onRepos() {
    this.isGroupsActive = false
    this.isRoleActive = false
    this.isReposActive = true;
  }

  onGroups() {
    this.isReposActive = false;
    this.isRoleActive = false
    this.isGroupsActive = true
  }

  onRole() {
    this.isReposActive = false;
    this.isGroupsActive = false
    this.isRoleActive = true
  }

  onSelectRepo(repo: string) {
    this.repoId = repo
  }

  onSelectGroup(group: string) {
    this.groupIds += (this.groupIds ? ';' : '') + group;
    this.isGroupsActive = false;
  }

  onSelectRole(role: string) {
    this.role = role;
    this.isGroupsActive = false;
  }

  splitGroups(group: string) {
    return group.split(";").map(group => group.trim()).filter(group => group.length > 0);
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('input.repoId')) {
      this.isReposActive = false;
    }
    if (!target.closest('input.groupId')) {
      this.isGroupsActive = false;
    }
    if (!target.closest('input.role')) {
      this.isRoleActive = false;
    }
  }

  @HostListener('document:keydown.tab', ['$event'])
  onEscapePress(event: KeyboardEvent) {
    this.isReposActive = false;
    this.isGroupsActive = false;
    this.isRoleActive = false;
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
