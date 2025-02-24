import {Component, OnInit} from '@angular/core';
import {NavbarComponent} from './navbar/navbar.component';
import {SidebarComponent} from './sidebar/sidebar.component';
import {FileService} from './service/file.service';
import {MarkdownModule} from 'ngx-markdown';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-main',
  imports: [
    NavbarComponent,
    SidebarComponent,
    MarkdownModule,
    RouterOutlet
  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.css'
})
export class MainComponent implements OnInit{

  constructor(public fileService: FileService) {
  }
  ngOnInit(): void {
    this.fileService.loadFiles();
  }

  selectFile(file: string): void {
    this.fileService.setSelectedFile(file);
  }
}
