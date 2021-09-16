import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {OutputProgrammeData} from '@cat/api';
import {HttpTestingController} from '@angular/common/http/testing';
import {ProgrammeModule} from '../programme.module';
import {TestModule} from '@common/test-module';
import {ProgrammeBasicDataComponent} from './programme-basic-data.component';

describe('ProgrammeBasicDataComponent', () => {
  let httpTestingController: HttpTestingController;
  let component: ProgrammeBasicDataComponent;
  let fixture: ComponentFixture<ProgrammeBasicDataComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProgrammeBasicDataComponent],
      imports: [
        ProgrammeModule,
        TestModule
      ],
    })
      .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProgrammeBasicDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update a programme', fakeAsync(() => {
    const user = {cci: 'some cci'} as OutputProgrammeData;

    component.saveProgrammeData$.next(user);
    let success = false;
    component.programmeSaveSuccess$.subscribe(result => success = result);

    httpTestingController.expectOne({method: 'GET', url: `//api/auth/current`});

    httpTestingController.expectOne({
      method: 'GET',
      url: `//api/programmedata`
    }).flush(user);

    httpTestingController.expectOne({
      method: 'PUT',
      url: `//api/programmedata`
    }).flush(user);
    httpTestingController.verify();

    tick(4100);
    expect(success).toBeTruthy();
  }));
});
