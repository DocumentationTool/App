import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceEditTagsComponent } from './resource-edit-tags.component';

describe('ResourceEditTagsComponent', () => {
  let component: ResourceEditTagsComponent;
  let fixture: ComponentFixture<ResourceEditTagsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceEditTagsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResourceEditTagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
