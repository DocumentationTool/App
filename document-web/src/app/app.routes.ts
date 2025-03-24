import {Routes} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {MainComponent} from './main/main.component';
import {AdminComponent} from './main/admin/admin.component';
import {EditorComponent} from './main/editor/editor.component';
import {ViewComponent} from './main/view/view.component';
import {NotFoundComponent} from './not-found/not-found.component';

export const routes: Routes = [
  {
    path: '', redirectTo: 'main',
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
        path: 'view',
        component: ViewComponent,
      },
      {
        path: 'editor',
        component: EditorComponent,
      },
    ],
  },
  {
    path: '**',
    component: NotFoundComponent
  }
];
