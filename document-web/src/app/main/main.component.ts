import {Component, OnInit} from '@angular/core';
import {NavbarComponent} from './navbar/navbar.component';
import {SidebarComponent} from './sidebar/sidebar.component';
import {NgIf, NgStyle} from '@angular/common';
import {FileService} from './service/file.service';
import {NavigationService} from './service/navigation.service';
import {MarkdownModule} from 'ngx-markdown';

@Component({
  selector: 'app-main',
  imports: [
    NavbarComponent,
    SidebarComponent,
    NgIf,
    NgStyle,
    MarkdownModule
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

  selectFile(file: string): void {
    this.fileService.setSelectedFile(file);
  }
}
