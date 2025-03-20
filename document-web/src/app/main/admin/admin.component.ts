import {Component, OnInit} from '@angular/core';
import {NavigationService} from '../service/navigation.service';
import {ApiRepo} from '../../api/apiRepo';
import {Repos} from '../../Model/apiResponseModelRepos';
import {UserService} from '../service/userService';
import {Router} from '@angular/router';

@Component({
  selector: 'app-admin',
  imports: [
  ],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit{

  constructor(private apiRepo: ApiRepo,
              private userService: UserService,
              private router: Router) {
  }

  allRepos: Repos[] = []

  ngOnInit() {
    this.apiRepo.getRepos().subscribe(
      data => {
        this.allRepos = data.content;
      }
    )
  }

  selectRepo(repo: Repos) {
    this.userService.selectedRepo.set(repo)
    this.router.navigate(['/main/repo'])
  }

}
