import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TreeChildComponent } from './tree-child.component';

describe('TreeChildComponent', () => {
  let component: TreeChildComponent;
  let fixture: ComponentFixture<TreeChildComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TreeChildComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TreeChildComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
