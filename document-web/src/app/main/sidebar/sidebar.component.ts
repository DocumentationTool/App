import {Component, OnInit} from '@angular/core';
import {NavigationService} from '../service/navigation.service';
import {KeyValuePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {ResourceService} from '../service/resource.service';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [

    NgClass,
    RouterLink,
    RouterLinkActive,
    NgForOf,
    KeyValuePipe,
    NgIf,
  ],
  templateUrl: './sidebar.component.html',
  standalone: true,
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit{
  openRepos: Set<string> = new Set();


  constructor(public navigationService: NavigationService,
              public resourceService: ResourceService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.resourceService.loadFileTree()
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

  upload() {
    this.resourceService.addResource();
  }

}
