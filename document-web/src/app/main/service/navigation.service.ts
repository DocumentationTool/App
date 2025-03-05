import {Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})

export class NavigationService{
  constructor(private router: Router) {
  }
  toggle = signal<boolean>(true);
  isAdmin= signal<boolean>(false);
  mode = signal<string>("editor");

  logAdminIn() {
    this.isAdmin.set(true);
  }

  logUserIn() {
    this.isAdmin.set(false);
  }

  toggleSidebar() {
    this.toggle.update(current => !current);
  }

  onEditor() {
    this.mode.set("editor")
  }

  onPreview() {
    this.mode.set("preview")
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

  isEditorOrPreviewActive(){
    return this.isEditorActive() || this.isPreviewActive()
  }
}
