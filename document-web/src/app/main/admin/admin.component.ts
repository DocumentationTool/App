import { Component } from '@angular/core';
import {NgStyle} from '@angular/common';
import {NavigationService} from '../service/navigation.service';

@Component({
  selector: 'app-admin',
  imports: [
    NgStyle
  ],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent {

  constructor(public navigationService: NavigationService) {

  }

}
