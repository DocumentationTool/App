import {Component, HostListener} from '@angular/core';
import {NavigationService} from '../service/navigation.service';
import {ResourceService} from '../service/resource.service';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {ContentGroup, Resources} from '../../Model/apiResponseFileTree';
import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {TreeChildComponent} from './tree-child/tree-child.component';


@Component({
  selector: 'app-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive,
    TreeChildComponent,
  ],
  templateUrl: './sidebar.component.html',
  standalone: true,
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  openRepos: Set<string> = new Set();


  constructor(public navigationService: NavigationService,
              public resourceService: ResourceService) {
  }

  repoId: string = ""
  path: string = ""
  createdBy: string = ""
  category: string | null = ""
  tagIds: string[] | null = [];
  data: string = "";

  onUpload(event: any) {
    if (this.resourceService.checkForFileChanges()) {
      if (window.confirm("Save changes?")) {
        this.resourceService.updateResource();
        this.upload(event);
      }
    } else {
      this.upload(event);
    }
    event.target.value = "";
  }

  upload(event: any) {
    const fileInput = event.target as HTMLInputElement;

    if (!fileInput.files || fileInput.files.length === 0) {
      console.warn("No resource selected.");
      return;
    }
    const file = fileInput.files[0];

    if (file.type !== "text/markdown" && !file.name.endsWith(".md")) {
      console.error("only .md files are allowed!");
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      this.data = reader.result as string;
      this.repoId = "repo2"
      this.path = file.name;
      this.createdBy = "niklas" //ToDo: Username
      this.category = null;
      this.tagIds = null;

      this.navigationService.uploadNewResource(this.data, this.path);
    };
    reader.readAsText(file);
  }

  loadNewResource(path: string, repoId: string, createdBy: string) {
    //gleich neue resource auswählen
    const newResource: Resources = {
      path: path,
      repoId: repoId,
      createdBy: createdBy,
      createdAt: "",
      category: "documents",
      tags: {},
      lastModifiedBy: "",
      lastModifiedAt: "",
      isEditable: true,
      data: ""
    };
    this.resourceService.selectResource(newResource)
  }
}
