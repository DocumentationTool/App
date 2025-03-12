import {Injectable, signal} from '@angular/core';
import {Router} from '@angular/router';
import {ApiUser} from '../../api/apiUser';
import {ApiAuth} from '../../api/apiAuth';

@Injectable({
  providedIn: 'root'
})

export class NavigationService{
  constructor(private router: Router,
              private apiAuth: ApiAuth) {
  }
  toggle = signal<boolean>(true);
  isAdmin= signal<boolean>(false);
  mode = signal<string>("editor");

  logAdminIn() {
    this.isAdmin.set(true);
  }

  logUserIn() {
    this.isAdmin.set(false);
    //ToDo: ausbauen:
    this.getTestUser();
  }

  getTestUser(){
    this.apiAuth.testLogin().subscribe(response => {
        console.log(response)
      },
      (error) => {
        console.error("Fehler Bei testLogin ", error)
      });
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
}
