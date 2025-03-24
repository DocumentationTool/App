import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import {importProvidersFrom, provideZoneChangeDetection} from '@angular/core';
import {provideRouter} from '@angular/router';
import {routes} from './app/app.routes';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {MarkdownModule} from 'ngx-markdown';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ToastrModule} from 'ngx-toastr';

bootstrapApplication(AppComponent, {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    importProvidersFrom(
      HttpClientModule,
      MarkdownModule.forRoot({ loader: HttpClient }),
      BrowserAnimationsModule,
      ToastrModule.forRoot({ // Globale Konfiguration
        positionClass: 'toast-bottom-right',
        timeOut: 3000,
        preventDuplicates: true
      })
    )
  ]
}).catch(err => console.error(err))
