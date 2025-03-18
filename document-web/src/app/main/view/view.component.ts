import {Component} from '@angular/core';
import {MarkdownComponent} from "ngx-markdown";
import {NgIf} from "@angular/common";
import {ResourceService} from '../service/resource.service';

@Component({
  selector: 'app-view',
  imports: [
    MarkdownComponent,
    NgIf
  ],
  templateUrl: './view.component.html',
  styleUrl: './view.component.css'
})
/**
 * Component shows resource preview
 */
export class ViewComponent {
  constructor(public resourceService: ResourceService) {

  }

}
