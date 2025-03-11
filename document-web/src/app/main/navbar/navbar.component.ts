import {Component, ElementRef, HostListener, OnInit, Resource, ViewChild} from '@angular/core';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import {NavigationService} from '../service/navigation.service';
import {FormsModule} from '@angular/forms';
import {ResourceService} from '../service/resource.service';
import {DocumentContentResponseModel} from '../../Model/DocumentContentResponseModel';
import {Resources} from '../../Model/apiResponseFileTree';

@Component({
  selector: 'app-navbar',
  imports: [
    RouterLinkActive,
    RouterLink,
    FormsModule,
  ],
  templateUrl: './navbar.component.html',
  standalone: true,
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit{
  constructor(public navigationService: NavigationService,
              private router: Router,
              public fileService: ResourceService) {
  }

  documents: DocumentContentResponseModel[] = [];

  ngOnInit() {
    this.fileService.loadFileTree();
  }

  onToggleSidebar() {
    this.navigationService.toggleSidebar()
  }

  // Searchbar
  searchTerm: string = '';
  filteredFiles: Resources[] = [];
  isSearchActive: boolean = false;
  @ViewChild('searchInput') searchInput!: ElementRef;

  search() {
    if (this.searchTerm.trim() === '') {
      this.filteredFiles = [];
      return;
    }

    const fileTree = this.fileService.fileTree()?.content;
    if (!fileTree) {
      this.filteredFiles = [];
      return;
    }

    this.filteredFiles = Object.entries(fileTree)
      .flatMap(([repoName, group]) =>
        group.resources.map(resource => ({ ...resource, repoName })) // Füge den Repo-Namen hinzu
      )
      .filter(resource => resource.path.toLowerCase().includes(this.searchTerm.toLowerCase()));

    this.isSearchActive = this.filteredFiles.length > 0;
  }


  onSearchFocus() {
    if (this.searchTerm.trim() !== '') {
      this.isSearchActive = true;
    }
    this.searchInput.nativeElement.focus();
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('form.search')) {
      this.isSearchActive = false;
    }
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscapePress(event: KeyboardEvent) {
    this.isSearchActive = false;
    this.searchInput.nativeElement.blur();
  }

  isDropdownOpen = false;

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }
}
