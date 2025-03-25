import {Component, HostListener, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ResourceService} from '../../service/resource.service';
import {NavigationService} from '../../service/navigation.service';
import { MatDialogRef } from '@angular/material/dialog';
import {ApiRepo} from '../../../api/apiRepo';
import {AuthService} from '../../service/authService';
import {ToastrService} from 'ngx-toastr';


@Component({
  selector: 'app-resource-upload',
  imports: [
    FormsModule,
  ],
  templateUrl: './resource-createNew.component.html',
  styleUrl: './resource-createNew.component.css'
})
export class ResourceCreateNewComponent implements OnInit{
  constructor(private dialogRef: MatDialogRef<ResourceCreateNewComponent>,
              public resourceService: ResourceService,
              private authService: AuthService,
              private apiRepo: ApiRepo) {}

  repoId: string = "";
  path: string = "";
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
      }
    )
  }

  uploadResource() {
    this.resourceService.addResource(this.repoId,this.path + ".md",this.authService.username(),this.category,this.tagIds,"# New Resource")
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
