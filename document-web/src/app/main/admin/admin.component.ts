import {Component, OnInit} from '@angular/core';
import {ApiRepo} from '../../api/apiRepo';
import {Repos} from '../../Model/apiResponseModelRepos';
import {NavigationService} from '../service/navigation.service';

@Component({
  selector: 'app-admin',
  imports: [
  ],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit{

  constructor(private apiRepo: ApiRepo,
              private navigationService: NavigationService) {
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
    this.navigationService.userManagement(repo);
  }

}
