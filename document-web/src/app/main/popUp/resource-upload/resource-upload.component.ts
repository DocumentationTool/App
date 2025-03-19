import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ResourceService} from '../../service/resource.service';
import {ApiRepo} from '../../../api/apiRepo';

@Component({
  selector: 'app-resource-upload',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './resource-upload.component.html',
  styleUrl: './resource-upload.component.css'
})
export class ResourceUploadComponent implements OnInit{
  constructor(private dialogRef: MatDialogRef<ResourceUploadComponent>,
              private resourceService: ResourceService,
              private apiRepo: ApiRepo,
              @Inject(MAT_DIALOG_DATA) public dialogData: { data: any; path: string }) {
  }

  repoId: string = "";
  category: string = "";
  tagIds: string[] = [];
  allRepos: string[] = [];

  isReposActive = false;

  ngOnInit() {
    this.apiRepo.getRepos().subscribe(
      data => {
        if (data && data.content) {
          this.allRepos = data.content.map(repo => repo.id);
        }
        console.log("Repos:", this.allRepos);      }
    )
  }

  uploadResource() {
    this.resourceService.addResource(this.repoId, this.dialogData.path, "niklas", this.category, this.tagIds, this.dialogData.data)
    this.closeDialog();
  }

  onRepos() {
    this.isReposActive = true;
  }

  onSelectRepo(repo:string){
    this.repoId = repo
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('input.repoId')) {
      this.isReposActive = false;
    }
  }

  @HostListener('document:keydown.tab', ['$event'])
  onEscapePress(event: KeyboardEvent) {
    this.isReposActive = false;
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
