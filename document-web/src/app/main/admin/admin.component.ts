import { Component } from '@angular/core';
import {NgStyle} from '@angular/common';
import {NavigationService} from '../service/navigation.service';

@Component({
  selector: 'app-admin',
  imports: [
    NgStyle
  ],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent {

  constructor(public navigationService: NavigationService) {

  }

  users = [{ name: 'Alice' }, { name: 'Bob' }, { name: 'Charlie' }];

  deleteUser(user: any) {
    console.log("delete user: ", user.name)
    // this.users.splice(index, 1);
  }

  addUser() {
    const newUserName = prompt('Enter new user name:');
    if (newUserName) {
      this.users.push({ name: newUserName });
    }
  }

  editUser(user: any) {
    console.log("edit user", user.name)
    // const updatedName = prompt('Edit user name:', this.users[index].name);
    // if (updatedName !== null && updatedName.trim() !== '') {
    //   this.users[index].name = updatedName;
    // }
  }

}
