import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceCreateNewComponent } from './resource-createNew.component';

describe('ResourceUploadComponent', () => {
  let component: ResourceCreateNewComponent;
  let fixture: ComponentFixture<ResourceCreateNewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceCreateNewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResourceCreateNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
