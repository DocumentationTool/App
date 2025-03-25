import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {GroupService} from '../../service/groupService';

@Component({
  selector: 'app-group-add',
  imports: [
    FormsModule
  ],
  templateUrl: './group-add.component.html',
  styleUrl: './group-add.component.css'
})
export class GroupAddComponent implements OnInit {

  constructor(private dialogRef: MatDialogRef<GroupAddComponent>,
              public groupService: GroupService){
  }

  groupIdToAdd: string = "";
  groupNameToAdd: string = "";
  groupIdToRemove: string = "";
  repoIdsToRemoveWindow = false;

  ngOnInit() {
    this.groupService.getGroup(null);
  }

  onEditGroup() {
    if (this.groupIdToAdd) {
      this.groupService.addGroup(this.groupIdToAdd, this.groupNameToAdd);
    }
    if (this.groupIdToRemove) {
      this.groupService.removeGroup(this.groupIdToRemove)
    }
    this.closeDialog();
  }

  selectGroup(GroupId: string) {
    this.groupIdToRemove += (this.groupIdToRemove ? ';' : '') + GroupId;
    this.repoIdsToRemoveWindow = false;
  }

  openDropdown() {
    this.repoIdsToRemoveWindow = true;
  }

  closeDialog() {
    this.dialogRef.close();
  }

  @HostListener('document:click', ['$event'])
  clickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('dropdown') && !target.closest('input.remove')) {
      this.repoIdsToRemoveWindow = false;
    }
  }
}
