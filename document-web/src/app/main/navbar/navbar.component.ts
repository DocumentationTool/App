import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import {NavigationService} from '../service/navigation.service';
import {FormsModule} from '@angular/forms';
import {FileService} from '../service/file.service';
import {ApiDocument} from '../../api/ApiDocument';
import {DocumentContent} from '../../Model/DocumentContent';

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
              public fileService: FileService,
              private apiDocument: ApiDocument ) {
  }

  documents: DocumentContent[] = [];


  ngOnInit() {
    this.fileService.loadFiles();
  }

  onGetDoc(){
    this.apiDocument.getDocument('repo1', 'user').subscribe(response => {
        this.documents = response.content;
        console.log('Erhaltene Dokumente:', this.documents);
    },
      (error) => {
        console.error("Fehler Bei getDocument ", error)
    });
  }

  onToggleSidebar() {
    this.navigationService.toggleSidebar()
  }

  // Searchbar
  searchTerm: string = '';
  filteredFiles: string[] = [];
  isSearchActive: boolean = false;
  @ViewChild('searchInput') searchInput!: ElementRef;

  search() {
    if (this.searchTerm.trim() === '') {
      this.filteredFiles = [];
      return;
    }

    this.filteredFiles = this.fileService.files().filter(file =>
      file.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
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
