import { Routes } from '@angular/router';
import {LoginComponent} from './login/login.component';
import {MainComponent} from './main/main.component';
import {AdminComponent} from './main/admin/admin.component';
import {EditorComponent} from './main/editor/editor.component';
import {PreviewComponent} from './main/preview/preview.component';

export const routes: Routes = [
  {
    path: '', redirectTo: 'login',
    pathMatch: "full"
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'main',
    component: MainComponent,
    children: [
      {
        path: 'admin',
        component: AdminComponent,
      },
      {
        path: 'editor',
        component: EditorComponent,
      },
      {
        path: 'preview',
        component: PreviewComponent,
      },
    ],
  }
];
