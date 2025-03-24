import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {NavigationService} from '../service/navigation.service';
import {FormsModule} from '@angular/forms';
import {ResourceService} from '../service/resource.service';
import {Resources} from '../../Model/apiResponseFileTree';
import {KeyValuePipe, NgClass, NgForOf} from '@angular/common';
import {AuthService} from '../service/authService';

@Component({
  selector: 'app-navbar',
  imports: [
    FormsModule,
    NgClass,
    NgForOf,
    KeyValuePipe,
  ],
  templateUrl: './navbar.component.html',
  standalone: true,
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  constructor(protected navigationService: NavigationService,
              protected resourceService: ResourceService,
              protected authService: AuthService) {
  }

  onToggleSidebar() {
    this.navigationService.toggleSidebar()
  }

  prefix: string = "$"
  searchTerm: string = '';
  filteredFiles: Resources[] = [];
  isSearchActive: boolean = false;
  isTagFilterActive: boolean = false;
  allTags: string[] | undefined = []
  whiteListTags: string[] = []
  blackListTags: string[] = []
  @ViewChild('searchInput') searchInput!: ElementRef;

  onTag(tag: string) {
    if (this.checkTag(tag) === "whiteList") {
      this.whiteListTags = this.whiteListTags.filter(t => t !== tag); // Entfernen aus whitelist
      this.blackListTags.push(tag); // Hinzufügen blacklist
    } else if (this.checkTag(tag) === "blackList") {
      this.blackListTags = this.blackListTags.filter(t => t !== tag); // Entfernen aus blacklist
    } else {
      this.whiteListTags.push(tag); // Tag zur Whitelist hinzufügen
    }
  }

  checkTag(tag: string) {
    if (this.whiteListTags.includes(tag)) {
      return "whiteList";
    }
    if (this.blackListTags.includes(tag)) {
      return "blackList";
    }
    return null;
  }

  onTagFilter() {
    this.isTagFilterActive = !this.isTagFilterActive;
    this.allTags = this.resourceService.getAllTags();
  }

  splitTagsIntoRows(tags: string[] | undefined, itemsPerRow: number): string[][] {
    let result: string[][] = [];
    if (tags) {
      for (let i = 0; i < tags.length; i += itemsPerRow) {
        result.push(tags.slice(i, i + itemsPerRow));
      }
    }

    return result;
  }

  dynamicCheckBox(tag: string) {
    if (this.checkTag(tag) == "whiteList") {
      return "ri-check-line"
    }
    if (this.checkTag(tag) == "blackList") {
      return "ri-close-line"
    }
    return "ri-checkbox-blank-line"
  }

  onSearchFocus() {
    this.isSearchActive = true;
    this.isTagFilterActive = false;
    this.searchInput.nativeElement.focus();
  }

  onSearchResource() {
    let inFileSearch: string;
    if (this.checkSearchType()) {
      inFileSearch = this.searchTerm.slice(1)
      this.resourceService.getResource(null, inFileSearch, null, this.authService.username(), this.whiteListTags, this.blackListTags);
    } else {
      this.resourceService.getResource(this.searchTerm, null, null, this.authService.username(), this.whiteListTags, this.blackListTags);
    }
    this.isSearchActive = true;
  }

  get searchResults() {
    return this.resourceService.searchResults();
  }

  checkSearchType() {
    return this.searchTerm.startsWith(this.prefix);
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
    this.isTagFilterActive = false
    this.searchInput.nativeElement.blur();
  }

  @HostListener('document:keydown.enter', ['$event'])
  onPressEnter(event: KeyboardEvent) {
    if (document.activeElement === this.searchInput.nativeElement) {
      this.onSearchResource();
    }
  }

  isDropdownOpen = false;

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }
}
