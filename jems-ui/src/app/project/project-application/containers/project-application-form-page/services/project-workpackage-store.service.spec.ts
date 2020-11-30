import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {TestModule} from '../../../../../common/test-module';
import {ProjectModule} from '../../../../project.module';
import {HttpTestingController} from '@angular/common/http/testing';
import {OutputWorkPackage} from '@cat/api';
import {ProjectWorkpackageStoreService} from './project-workpackage-store.service';

describe('ProjectWorkpackageStoreService', () => {
  let service: ProjectWorkpackageStoreService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        TestModule,
        ProjectModule,
      ],
    });
    service = TestBed.inject(ProjectWorkpackageStoreService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch the initial workpackage', fakeAsync(() => {
    let resultWorkPackage;
    service.workPackage$.subscribe(result => resultWorkPackage = result as OutputWorkPackage);
    service.init(2, 1);

    httpTestingController.match({method: 'GET', url: `//api/project/1/workpackage/2`})
      .forEach(req => req.flush({id: 2}));

    tick();
    expect(resultWorkPackage).toEqual({id: 2} as any);
  }));
});