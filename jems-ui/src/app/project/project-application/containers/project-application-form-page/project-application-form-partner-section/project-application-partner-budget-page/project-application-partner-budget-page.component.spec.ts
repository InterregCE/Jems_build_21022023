import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {ProjectApplicationPartnerBudgetPageComponent} from './project-application-partner-budget-page.component';
import {TestModule} from '../../../../../../common/test-module';
import {ProjectModule} from '../../../../../project.module';
import {HttpTestingController} from '@angular/common/http/testing';
import {ActivatedRoute} from '@angular/router';
import {PartnerBudgetTable} from '../../../../model/partner-budget-table';

describe('ProjectApplicationPartnerBudgetPageComponent', () => {
  let component: ProjectApplicationPartnerBudgetPageComponent;
  let fixture: ComponentFixture<ProjectApplicationPartnerBudgetPageComponent>;

  let httpTestingController: HttpTestingController;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TestModule,
        ProjectModule
      ],
      declarations: [ProjectApplicationPartnerBudgetPageComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {params: {projectId: 1, partnerId: 2}}
          }
        }
      ]
    })
      .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectApplicationPartnerBudgetPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch and save budgets', fakeAsync(() => {
    httpTestingController.expectOne({
      method: 'GET',
      url: '//api/project/1/partner/2/staffcost'
    });
    httpTestingController.expectOne({
      method: 'GET',
      url: '//api/project/1/partner/2/travel'
    })
    httpTestingController.expectOne({
      method: 'GET',
      url: '//api/project/1/partner/2/external'
    })
    httpTestingController.expectOne({
      method: 'GET',
      url: '//api/project/1/partner/2/equipment'
    })
    httpTestingController.expectOne({
      method: 'GET',
      url: '//api/project/1/partner/2/infrastructure'
    });

    component.saveBudgets$.next({
      staff: new PartnerBudgetTable([]),
      travel: new PartnerBudgetTable([]),
      external: new PartnerBudgetTable([]),
      equipment: new PartnerBudgetTable([]),
      infrastructure: new PartnerBudgetTable([])
    });
    tick();

    httpTestingController.expectOne({
      method: 'PUT',
      url: '//api/project/1/partner/2/staffcost'
    });
    httpTestingController.expectOne({
      method: 'PUT',
      url: '//api/project/1/partner/2/travel'
    })
    httpTestingController.expectOne({
      method: 'PUT',
      url: '//api/project/1/partner/2/external'
    })
    httpTestingController.expectOne({
      method: 'PUT',
      url: '//api/project/1/partner/2/equipment'
    })
    httpTestingController.expectOne({
      method: 'PUT',
      url: '//api/project/1/partner/2/infrastructure'
    });
  }));

});