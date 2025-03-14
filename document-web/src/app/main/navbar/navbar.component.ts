import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {NavigationService} from '../service/navigation.service';
import {FormsModule} from '@angular/forms';
import {ResourceService} from '../service/resource.service';
import {Resources} from '../../Model/apiResponseFileTree';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-navbar',
  imports: [
    FormsModule,
    NgClass,
  ],
  templateUrl: './navbar.component.html',
  standalone: true,
  styleUrl: './navbar.component.css'
})
export class NavbarComponent{
  constructor(public navigationService: NavigationService,
              public resourceService: ResourceService) {
  }

  onToggleSidebar() {
    this.navigationService.toggleSidebar()
  }

  // Searchbar
  searchTerm: string = '';
  filteredFiles: Resources[] = [];
  isSearchActive: boolean = false;
  isFilterActive: boolean = false;
  isTagFilterActive: boolean = false;
  @ViewChild('searchInput') searchInput!: ElementRef;

  search() {
    if (this.searchTerm.trim() === '') {
      this.filteredFiles = [];
      return;
    }

    const searchLower = this.searchTerm.toLowerCase();

    // Filtere die Ressourcen basierend auf Path, RepoId und Tags
    this.filteredFiles = [];

    // Durchlaufe alle Repositories und Ressourcen
    Object.values(this.resourceService.fileTree()?.content || {}).forEach(group => {
      group.resources.forEach(resource => {
        // Prüfe, ob der searchTerm im Path, RepoId oder einem Tag enthalten ist
        const pathMatch = resource.path.toLowerCase().includes(searchLower);
        const repoIdMatch = resource.repoId.toLowerCase().includes(searchLower);
        const tagMatch = Object.values(resource.tags).some(tag => tag.toLowerCase().includes(searchLower));

        if (pathMatch || repoIdMatch || tagMatch) {
          this.filteredFiles.push(resource);
        }
      });
    });

    this.isSearchActive = this.filteredFiles.length > 0;
  }

  onFilter() {
    this.isFilterActive = !this.isFilterActive;
    this.isTagFilterActive = false;

  }

  onTags(){
    this.isFilterActive = false;
    this.isTagFilterActive = true;
  }

  onSearchFocus() {
    if (this.searchTerm.trim() !== '') {
      this.isSearchActive = true;
    }
    this.isFilterActive = false;
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
