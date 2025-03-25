import {Component, HostListener} from '@angular/core';
import {NavigationService} from '../service/navigation.service';
import {ResourceService} from '../service/resource.service';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {TreeChildComponent} from './tree-child/tree-child.component';
import * as Mammoth from 'mammoth';
import TurndownService from 'turndown';
import {ToastrService} from 'ngx-toastr';
import {AuthService} from '../service/authService';
import {KeyValuePipe, NgClass, NgForOf} from '@angular/common';
import {Resources} from '../../Model/apiResponseFileTree';


@Component({
  selector: 'app-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive,
    TreeChildComponent,
    KeyValuePipe,
    NgForOf,
    NgClass,
  ],
  templateUrl: './sidebar.component.html',
  standalone: true,
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  openRepos: Set<string> = new Set();


  constructor(protected navigationService: NavigationService,
              protected authService: AuthService,
              protected resourceService: ResourceService,
              private toastr: ToastrService) {
  }

  repoId: string = ""
  path: string = ""
  createdBy: string = ""
  category: string | null = ""
  tagIds: string[] | null = [];
  data: string = "";

  hoveredRepo: any;
  selectedRepo: string | null = null;
  selectedResource: Resources | null = null;
  hoveredResource: any;
  menuPosition = {x: 0, y: 0};
  isOpen: Record<string, boolean> = {};

  openResourceMenu(event: MouseEvent, resource: Resources) {
    event.stopPropagation();
    this.selectedResource = resource;
    this.menuPosition = {x: this.popUpInWindow(event.clientX -110), y: event.clientY + 10};
  }

  deleteResource(resource: Resources) {
    if (window.confirm("Do you really want to delete '" + resource.path + "' in Repo: '" + resource.repoId + "'?")) {
      this.resourceService.removeResource(resource.repoId, resource.path);
      this.resourceService.loadFileTree();
    }
  }

  popUpInWindow(posX: number) {
    if (posX < 30){
      return 30
    }
    return posX
  }


  openRepoMenu(event: MouseEvent, repoId: string){
    event.stopPropagation();
    this.selectedRepo = repoId;
    this.menuPosition = {x: this.popUpInWindow(event.clientX -110), y: event.clientY + 10};
  }

  toggleRepo(repoPath: string) {
    this.isOpen[repoPath] = !this.isOpen[repoPath]; // Öffnen/Schließen der Unter-Repositories
  }

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

    if (file.type === "text/markdown" || file.name.endsWith(".md")) {
      this.readMarkdownFile(file);
    } else if (file.type === "application/vnd.openxmlformats-officedocument.wordprocessingml.document" || file.name.endsWith(".docx")) {
      this.convertDocxToMarkdown(file);
    } else {
      console.error("Only .md and .docx files are allowed!");
    }
  }

// Liest eine Markdown-Datei direkt
  private readMarkdownFile(file: File) {
    const reader = new FileReader();
    reader.onload = () => {
      this.processFileContent(reader.result as string, file.name);
    };
    reader.readAsText(file);
  }

// Konvertiert eine .docx-Datei erst zu HTML, dann zu Markdown
  private convertDocxToMarkdown(file: File) {
    const reader = new FileReader();
    reader.onload = async (event: any) => {
      const arrayBuffer = event.target.result;

      // Ignoriere Bilder und konvertiere nur den Text
      const options = {
        convertImage: Mammoth.images.imgElement(() => Promise.resolve({ src: "" }))
      };

      Mammoth.convertToHtml({ arrayBuffer }, options)
        .then((result) => {
          // HTML zu Markdown konvertieren
          const turndownService = new TurndownService();
          const markdown = turndownService.turndown(result.value);
          this.processFileContent(markdown, file.name.replace(".docx", ".md"));
        })
        .catch((error) => this.toastr.error("Error converting .docx: " + error.message));
    };
    reader.readAsArrayBuffer(file);
  }

// Verarbeitet die Datei-Inhalte und führt den Upload durch
  private processFileContent(content: string, fileName: string) {
    this.data = content;
    this.repoId = "repo2";
    this.path = fileName;
    this.createdBy = this.authService.username() ;
    this.category = null;
    this.tagIds = null;

    this.navigationService.uploadNewResource(this.data, this.path);
  }

  @HostListener('document:click', ['$event'])
  closeMenu(_: Event) {
    this.selectedRepo = null
    this.selectedResource = null
  }

}
