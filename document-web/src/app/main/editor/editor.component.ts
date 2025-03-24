import {Component, HostListener, OnDestroy} from '@angular/core';
import {ResourceService} from '../service/resource.service';
import {MarkdownModule} from 'ngx-markdown';
import {LMarkdownEditorModule, UploadResult} from 'ngx-markdown-editor';
import {FormsModule} from '@angular/forms';
import {NavigationService} from '../service/navigation.service';

@Component({
  selector: 'app-editor',
  imports: [
    MarkdownModule,
    LMarkdownEditorModule,
    FormsModule,
  ],
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent implements OnDestroy {
  constructor(public resourceService: ResourceService,
              public navigationService: NavigationService) {
    this.doUpload = this.doUpload.bind(this);
  }

  onInput(event: Event) {
    console.log("tst", event)
  }

  onEditorLoaded(editor: { setShowPrintMargin: (arg0: boolean) => void; }) {
    editor.setShowPrintMargin(false)
  }

  onPreviewDomChanged(dom: HTMLElement) {
    console.log(`onPreviewDomChanged fired`);
  }

  doUpload(files: Array<File>): Promise<Array<UploadResult>> {
    // do upload file by yourself
    return Promise.resolve([{name: 'xxx', url: 'xxx.png', isImg: true}]);
  }

  preRenderFunc(content: string) {
    return content.replace(/something/g, 'new value'); // must return a string
  }

  postRenderFunc(content: string) {
    return content.replace(/something/g, 'new value'); // must return a string
  }

  canDeactivate(): boolean {
    if (this.resourceService.checkForFileChanges()) {
      const confirmed = window.confirm('Save changes?');
      if (confirmed) {
        this.resourceService.updateResource();
      }
      return confirmed;
    }
    return true;
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any): void {
    if (this.resourceService.checkForFileChanges()) {
      $event.returnValue = true;
    }
  }

  ngOnDestroy() {
    this.resourceService.removeFileEditing();
  }
}
