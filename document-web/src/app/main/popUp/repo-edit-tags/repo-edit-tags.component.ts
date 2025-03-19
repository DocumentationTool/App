import {Component, ElementRef, HostListener, Inject, OnInit, ViewChild} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ResourceService} from '../../service/resource.service';
import {NavigationService} from '../../service/navigation.service';
import {ApiRepo} from '../../../api/apiRepo';

@Component({
  selector: 'app-repo-edit-tags',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './repo-edit-tags.component.html',
  styleUrl: './repo-edit-tags.component.css'
})
export class RepoEditTagsComponent implements OnInit {
  constructor(private dialogRef: MatDialogRef<RepoEditTagsComponent>,
              public resourceService: ResourceService,
              @Inject(MAT_DIALOG_DATA) public dialogData: { repoId: string }) {
  }

  tagIdsToAdd: string = "";
  tagNamesToAdd: string = "";
  tagIdsToRemove: string = "";
  tagIdsToRemoveWindow = false;

  ngOnInit() {
    this.resourceService.getTag(this.dialogData.repoId);

  }

  onEditTags() {
    console.log(this.splitTags(this.tagIdsToRemove))
    this.resourceService.editRepoTags(this.dialogData.repoId, this.splitTags(this.tagIdsToAdd), this.splitTags(this.tagNamesToAdd), this.splitTags(this.tagIdsToRemove))
    this.closeDialog();
  }

  selectTag(tagId: string) {
    this.tagIdsToRemove += (this.tagIdsToRemove ? ';' : '') + tagId;
    this.tagIdsToRemoveWindow = false;
  }

  openDropdown() {
    this.tagIdsToRemoveWindow = true;
  }


  splitTags(tags: string
  ) {
    return tags.split(";").map(tag => tag.trim()).filter(tag => tag.length > 0);
  }

  joinTags(tags: string[]
  ) {
    if (tags)
      return tags.filter(tag => tag.length > 0).map(tag => tag.trim()).join(";");
    return ""
  }

  closeDialog() {
    this.dialogRef.close();
  }

  @HostListener('document:click', ['$event'])
  clickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('dropdown') && !target.closest('input.remove')) {
      this.tagIdsToRemoveWindow = false;
    }
  }
}
