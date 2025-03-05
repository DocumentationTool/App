import {Component, OnDestroy} from '@angular/core';
import {FileService} from '../service/file.service';
import {MarkdownModule} from 'ngx-markdown';
import {LMarkdownEditorModule, UploadResult} from 'ngx-markdown-editor';
import {FormsModule} from '@angular/forms';
import {NavigationService} from '../service/navigation.service';
import {EmptyPageComponent} from '../empty-page/empty-page.component';

@Component({
  selector: 'app-editor',
  imports: [
    MarkdownModule,
    LMarkdownEditorModule,
    FormsModule,
    EmptyPageComponent,
  ],
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent implements OnDestroy {
  constructor(public fileService: FileService,
              public navigationService: NavigationService) {
    this.doUpload = this.doUpload.bind(this);
  }

  onInput(event: Event) {
    console.log("tst", event)
  }

  onEditorLoaded(editor: { setShowPrintMargin: (arg0: boolean) => void; }) {
    console.log(`ACE Editor Ins: `, editor);

    editor.setShowPrintMargin(false)
  }

  onPreviewDomChanged(dom: HTMLElement) {
    console.log(`onPreviewDomChanged fired`);
  }

  //@ts-ignore
  uploadImg(evt) {
    if (!evt) return;
    const file = evt.target.files[0];
    const reader = new FileReader();
    reader.addEventListener("load", () => {
      this.fileService.fileContent.set(this.fileService.fileContent() + `![](${reader.result})`);
    }, false);

    if (file) reader.readAsDataURL(file);
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

  ngOnDestroy() {
    //Check File change
    this.fileService.checkForFileChanges();
  }

}
