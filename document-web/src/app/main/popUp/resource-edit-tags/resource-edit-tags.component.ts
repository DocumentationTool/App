import {Component, ElementRef, HostListener, Inject, OnInit, ViewChild} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ResourceService} from '../../service/resource.service';
import {NavigationService} from '../../service/navigation.service';
import {ApiRepo} from '../../../api/apiRepo';
import {Resources} from '../../../Model/apiResponseFileTree';

@Component({
  selector: 'app-resource-edit-tags',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './resource-edit-tags.component.html',
  styleUrl: './resource-edit-tags.component.css'
})
export class ResourceEditTagsComponent implements OnInit{
  constructor(private dialogRef: MatDialogRef<ResourceEditTagsComponent>,
              public resourceService: ResourceService,
              @Inject(MAT_DIALOG_DATA) public dialogData: { repoId: string, path: string }) {
  }

  tagsToAdd: string = "";
  tagIdsToRemove: string = "";
  tagIdsToRemoveWindow = false;
  tagIdsToAddWindow = false;

  ngOnInit() {
    this.resourceService.getTag(this.dialogData.repoId);
    this.resourceService.getResourceTags(null,this.dialogData.path, this.dialogData.repoId,null,[],[])
  }

  onEditTags() {
    this.resourceService.editResourceTags(this.dialogData.repoId, this.dialogData.path, this.splitTags(this.tagsToAdd), this.splitTags(this.tagIdsToRemove))
    this.closeDialog();
  }

  selectAddTag(tagId: string) {
    this.tagsToAdd += (this.tagsToAdd ? ';' : '') + tagId;
    this.tagIdsToAddWindow = false;
  }

  selectRemoveTag(tagId: string) {
    this.tagIdsToRemove += (this.tagIdsToRemove ? ';' : '') + tagId;
    this.tagIdsToRemoveWindow = false
  }

  openAddDropdown() {
    this.tagIdsToRemoveWindow = false;
    this.tagIdsToAddWindow = true;
  }

  openRemoveDropdown(){
    this.tagIdsToAddWindow = false;
    this.tagIdsToRemoveWindow = true
  }

  splitTags(tags: string) {
    return tags.split(";").map(tag => tag.trim()).filter(tag => tag.length > 0);
  }

  @HostListener('document:click', ['$event'])
  clickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('dropdown') && !target.closest('input.add')) {
      this.tagIdsToAddWindow = false;
    }
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
