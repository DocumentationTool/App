import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RepoEditTagsComponent } from './repo-edit-tags.component';

describe('RepoEditTagsComponent', () => {
  let component: RepoEditTagsComponent;
  let fixture: ComponentFixture<RepoEditTagsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RepoEditTagsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RepoEditTagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
