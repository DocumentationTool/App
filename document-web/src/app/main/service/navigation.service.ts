import {Injectable, signal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})

export class NavigationService{
  toggle = signal<boolean>(true);
  isAdmin= signal<boolean>(false)

  logAdminIn() {
    this.isAdmin.set(true);
  }

  logUserIn() {
    this.isAdmin.set(false);
  }

  toggleSidebar() {
    this.toggle.update(current => !current);
  }
}
