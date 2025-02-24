import { Routes } from '@angular/router';
import {LoginComponent} from './login/login.component';
import {MainComponent} from './main/main.component';
import {AdminComponent} from './main/admin/admin.component';
import {EditorComponent} from './main/editor/editor.component';

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
        path: 'admin', // child route path
        component: AdminComponent, // child route component that the router renders
      },
      {
        path: 'editor',
        component: EditorComponent, // another child route component that the router renders
      },
    ],
  }
];
