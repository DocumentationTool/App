import {Component, OnInit} from '@angular/core';
import {NavbarComponent} from './navbar/navbar.component';
import {SidebarComponent} from './sidebar/sidebar.component';
import {FileService} from './service/file.service';
import {MarkdownModule} from 'ngx-markdown';
import {RouterOutlet} from '@angular/router';
import {NgClass} from '@angular/common';
import {NavigationService} from './service/navigation.service';

@Component({
  selector: 'app-main',
  imports: [
    NavbarComponent,
    SidebarComponent,
    MarkdownModule,
    RouterOutlet,
    NgClass
  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.css'
})
export class MainComponent implements OnInit{

  constructor(public fileService: FileService,
              public navigationService: NavigationService) {

  }
  ngOnInit(): void {
    this.fileService.loadFiles();
  }

}
