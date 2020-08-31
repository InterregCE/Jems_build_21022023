import {async, ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import { ProgrammeOutputIndicatorSubmissionPageComponent } from './programme-output-indicator-submission-page.component';
import {InputIndicatorOutputCreate, InputIndicatorOutputUpdate} from '@cat/api';
import {HttpTestingController} from '@angular/common/http/testing';
import {TestModule} from '../../../../common/test-module';
import {ProgrammeModule} from '../../../programme.module';

describe('ProgrammeFinalIndicatorSubmissionPageComponent', () => {
  let component: ProgrammeOutputIndicatorSubmissionPageComponent;
  let fixture: ComponentFixture<ProgrammeOutputIndicatorSubmissionPageComponent>;
  let httpTestingController: HttpTestingController;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TestModule,
        ProgrammeModule
      ],
      declarations: [ ProgrammeOutputIndicatorSubmissionPageComponent ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProgrammeOutputIndicatorSubmissionPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should update an indicator', fakeAsync(() => {
    component.updateOutputIndicator({} as InputIndicatorOutputUpdate);

    httpTestingController.expectOne({
      method: 'PUT',
      url: `//api/programmeindicator/output`
    })
  }));

  it('should create a call', fakeAsync(() => {
    component.createOutputIndicator({} as InputIndicatorOutputCreate);

    httpTestingController.expectOne({
      method: 'POST',
      url: `//api/programmeindicator/output`
    })
  }));
});