import { Component } from '@angular/core';
import {MarkdownComponent} from "ngx-markdown";
import {NgIf} from "@angular/common";
import {FileService} from '../service/file.service';

@Component({
  selector: 'app-view',
  imports: [
    MarkdownComponent,
    NgIf
  ],
  templateUrl: './view.component.html',
  styleUrl: './view.component.css'
})
export class ViewComponent {
  constructor(public fileService: FileService) {

  }
}
