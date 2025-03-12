import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { Observable } from 'rxjs';

// Interface, das Komponenten implementieren können, die eine Deaktivierung bestätigen sollen
export interface CanComponentDeactivate {
  canDeactivate: () => Observable<boolean> | Promise<boolean> | boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ConfirmDeactivateGuard implements CanDeactivate<CanComponentDeactivate> {
  canDeactivate(component: CanComponentDeactivate): Observable<boolean> | Promise<boolean> | boolean {
    // Ruft die canDeactivate-Methode der Komponente auf, falls vorhanden
    return component.canDeactivate ? component.canDeactivate() : true;
  }
}
