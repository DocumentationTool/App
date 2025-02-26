import {Component, OnInit} from '@angular/core';
import {NavigationService} from '../service/navigation.service';
import {NgClass} from '@angular/common';
import {FileService} from '../service/file.service';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';

@Component({
  selector: 'app-sidebar',
    imports: [

        NgClass,
        RouterLink,
        RouterLinkActive
    ],
  templateUrl: './sidebar.component.html',
  standalone: true,
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit{

  constructor(public navigationService: NavigationService,
              public fileService: FileService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.fileService.loadFiles()
  }

  selectFile(file: string): void {
    this.fileService.setSelectedFile(file)
    this.router.navigate(['/main/view']);
  }

  getAssetUrl(file: string): string {
    return file;
  }

}
