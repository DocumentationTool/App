import { Component } from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {NavigationService} from '../service/navigation.service';

@Component({
  selector: 'app-navbar',
  imports: [
    RouterLinkActive,
    RouterLink
  ],
  templateUrl: './navbar.component.html',
  standalone: true,
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  constructor(public navigationService: NavigationService) {
  }

  onToggle(){
    this.navigationService.toggleValue()
  }

}
