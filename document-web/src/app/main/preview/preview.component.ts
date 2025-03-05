import { Component } from '@angular/core';
import {MarkdownComponent} from "ngx-markdown";
import {NgIf} from "@angular/common";
import {FileService} from '../service/file.service';

@Component({
  selector: 'app-preview',
  imports: [
    MarkdownComponent,
    NgIf
  ],
  templateUrl: './preview.component.html',
  styleUrl: './preview.component.css'
})
export class PreviewComponent {
  constructor(public fileService: FileService) {

  }
}
