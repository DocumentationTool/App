import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceMoveComponent } from './resource-move.component';

describe('ResourceMoveComponent', () => {
  let component: ResourceMoveComponent;
  let fixture: ComponentFixture<ResourceMoveComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceMoveComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResourceMoveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
