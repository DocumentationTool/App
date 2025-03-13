import {Component, HostListener} from '@angular/core';
import {NavigationService} from '../service/navigation.service';
import {KeyValue, KeyValuePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {ResourceService} from '../service/resource.service';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {ContentGroup, Resources} from '../../Model/apiResponseFileTree';
import { CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {ResourceUploadComponent} from '../popUp/resource-upload/resource-upload.component';

@Component({
  selector: 'app-sidebar',
  imports: [

    NgClass,
    RouterLink,
    RouterLinkActive,
    NgForOf,
    KeyValuePipe,
    NgIf,
    ResourceUploadComponent,

  ],
  templateUrl: './sidebar.component.html',
  standalone: true,
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  openRepos: Set<string> = new Set();
  menuPosition = { x: 0, y: 0 };
  selectedResource: Resources | null = null;

  constructor(public navigationService: NavigationService,
              public resourceService: ResourceService) {
  }

  toggleRepo(repoKey: string) {
    if (this.openRepos.has(repoKey)) {
      this.openRepos.delete(repoKey); // Repository schließen
    } else {
      this.openRepos.add(repoKey); // Repository öffnen
    }
  }

  // Methode, um zu prüfen, ob ein Repository geöffnet ist
  isRepoOpen(repoKey: string): boolean {
    return this.openRepos.has(repoKey);
  }

  getAssetUrl(file: string): string {
    return file;
  }

  repoId: string = ""
  path: string = ""
  createdBy: string = ""
  category: string | null = ""
  tagIds: string[] | null = [];
  data: string = "";
  hoveredResource: any;

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

      this.resourceService.addResource(this.repoId, this.path, this.createdBy, this.category, this.tagIds, this.data)
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

  openMenu(event: MouseEvent, resource: Resources) {
    event.stopPropagation();
    this.selectedResource = resource;
    this.menuPosition = { x: event.clientX, y: event.clientY };

  }

  // Schließt das Menü, wenn irgendwo anders geklickt wird
  @ HostListener('document:click', ['$event'])
  closeMenu(event: Event) {
    this.selectedResource = null;
  }

// Beispielaktionen für Menüoptionen
  editTags(resource: Resources) {
    console.log("Bearbeiten:", resource);
  }

  deleteResource(resource: Resources) {
    if (window.confirm("Do you really want to delete '" + resource.path + "' in Repo: '" + resource.repoId + "'?")){
      this.resourceService.removeResource(resource.repoId, resource.path);
      this.resourceService.loadFileTree();
    }
  }

  moveResource(resource: Resources) {

  }





  onDrop(event: CdkDragDrop<any>, targetRepoKey: string, sourceRepo:string) {
    const draggedItem = event; // Das gezogene File
    const previousRepo = event.previousContainer.data; // Das vorherige Repo
    const newRepo = targetRepoKey; // Das Ziel-Repo

    console.log("📦 Dragged File:", draggedItem);
    console.log("⬅️ From:", previousRepo);
    console.log("➡️ To:", newRepo);

    // Wenn das Ziel ein anderes Repo ist, verschiebe die Datei
    if (event.previousContainer !== event.container) {
      // this.resourceService.moveResource(draggedItem, previousRepo, newRepo);
      console.log("Auf repo")
    } else {
      // Falls innerhalb des gleichen Repos bewegt, neu anordnen
      // moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      console.log("auf resource")
    }
  }
}
