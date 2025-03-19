import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceUploadComponent } from './resource-upload.component';

describe('ResourceUploadComponent', () => {
  let component: ResourceUploadComponent;
  let fixture: ComponentFixture<ResourceUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceUploadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResourceUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
