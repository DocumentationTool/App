import {Injectable, signal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})

export class NavigationService{
  toggle = signal<boolean>(false);

  setToggle(value: boolean) {
    this.toggle.set(value);
  }

  toggleValue() {
    this.toggle.update(current => !current);
  }
}
