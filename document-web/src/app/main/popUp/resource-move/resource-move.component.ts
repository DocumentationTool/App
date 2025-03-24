import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ResourceService} from '../../service/resource.service';
import {NavigationService} from '../../service/navigation.service';
import {ApiRepo} from '../../../api/apiRepo';
import {AuthService} from '../../service/authService';

@Component({
  selector: 'app-resource-move',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './resource-move.component.html',
  styleUrl: './resource-move.component.css'
})
export class ResourceMoveComponent implements OnInit{
  constructor(private dialogRef: MatDialogRef<ResourceMoveComponent>,
              protected resourceService: ResourceService,
              private authService: AuthService,
              @Inject(MAT_DIALOG_DATA) public dialogData: { repoId: string, path: string }) {
  }

  ngOnInit() {
    this.repoTo = this.dialogData.repoId;
    this.pathTo = this.dialogData.path;
  }

  repoTo: string = "";
  pathTo: string = "";

  moveResource() {
    this.resourceService.moveResource(this.authService.username(), this.dialogData.repoId,this.dialogData.path,this.repoTo, this.pathTo)
    this.closeDialog();
  }

  closeDialog() {
    this.dialogRef.close();
  }

}
