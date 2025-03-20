import { Routes } from '@angular/router';
import {LoginComponent} from './login/login.component';
import {MainComponent} from './main/main.component';
import {AdminComponent} from './main/admin/admin.component';
import {EditorComponent} from './main/editor/editor.component';
import {ViewComponent} from './main/view/view.component';
import {EmptyPageComponent} from './main/empty-page/empty-page.component';
import {RepoComponent} from './main/admin/repo/repo.component';

export const routes: Routes = [
  {
    path: '', redirectTo: 'main/empty',
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
        path: 'repo',
        component: RepoComponent,
      },
      {
        path: 'view',
        component: ViewComponent,
      },
      {
        path: 'editor',
        component: EditorComponent,
      },
      {
        path: 'empty',
        component: EmptyPageComponent,
      },
    ],
  }
];
