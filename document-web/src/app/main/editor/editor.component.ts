import { Component } from '@angular/core';
import {FileService} from '../service/file.service';
import {MarkdownModule} from 'ngx-markdown';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-editor',
  imports: [
    MarkdownModule,
    NgIf,
  ],
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent {
  constructor(public fileService: FileService) {

  }
}
