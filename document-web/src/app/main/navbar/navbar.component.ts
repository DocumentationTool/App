import { Component } from '@angular/core';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import {NavigationService} from '../service/navigation.service';
import {FormsModule} from '@angular/forms';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-navbar',
  imports: [
    RouterLinkActive,
    RouterLink,
    FormsModule,
    NgIf,
    NgForOf
  ],
  templateUrl: './navbar.component.html',
  standalone: true,
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  constructor(public navigationService: NavigationService,
              private router: Router) {
  }

  onToggle(){
    this.navigationService.toggleValue()
  }

  isEditorActive(): boolean {
    return this.router.isActive('/main/editor', {
      paths: 'exact',
      queryParams: 'ignored',
      fragment: 'ignored',
      matrixParams: 'ignored',
    });
  }

  isPreviewActive(): boolean {
    return this.router.isActive('/main/preview', {
      paths: 'exact',
      queryParams: 'ignored',
      fragment: 'ignored',
      matrixParams: 'ignored',
    })
  }

  searchTerm: string = '';
  files: string[] = ['douk1.md', 'doku2.md'];
  filteredFiles: string[] = [];

  search() {
    if (this.searchTerm.trim() === '') {
      this.filteredFiles = [];
      return;
    }

    this.filteredFiles = this.files.filter(file =>
      file.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

}
