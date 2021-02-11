import {ChangeDetectionStrategy, Component} from '@angular/core';
import {combineLatest, Subject} from 'rxjs';
import {MatSort} from '@angular/material/sort';
import {map, mergeMap, startWith, take, tap} from 'rxjs/operators';
import {Tables} from '../../../../../common/utils/tables';
import {Log} from '../../../../../common/utils/log';
import {ProjectPartnerService} from '@cat/api';
import {Permission} from '../../../../../security/permissions/permission';
import {ProjectApplicationFormSidenavService} from '../services/project-application-form-sidenav.service';
import {ActivatedRoute} from '@angular/router';
import {ProjectStore} from '../../project-application-detail/services/project-store.service';

@Component({
  selector: 'app-project-application-form-partner-section',
  templateUrl: './project-application-form-partner-section.component.html',
  styleUrls: ['./project-application-form-partner-section.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFormPartnerSectionComponent {
  Permission = Permission;

  projectId = this.activatedRoute?.snapshot?.params?.projectId;

  newPageSize$ = new Subject<number>();
  newPageIndex$ = new Subject<number>();
  newSort$ = new Subject<Partial<MatSort>>();

  partnerPage$ =
    combineLatest([
      this.newPageIndex$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_INDEX)),
      this.newPageSize$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_SIZE)),
      this.newSort$.pipe(
        startWith({active: 'sortNumber', direction: 'asc'}),
        map(sort => sort?.direction ? sort : {active: 'sortNumber', direction: 'asc'}),
        map(sort => `${sort.active},${sort.direction}`)
      )
    ])
      .pipe(
        mergeMap(([pageIndex, pageSize, sort]) =>
          // put lead partner on top by default
          this.projectPartnerService.getProjectPartners(this.projectId, pageIndex, pageSize, ['role,asc', sort])),
        tap(page => Log.info('Fetched the project partners:', this, page.content)),
      );

  constructor(public projectStore: ProjectStore,
              private projectPartnerService: ProjectPartnerService,
              private projectApplicationFormSidenavService: ProjectApplicationFormSidenavService,
              private activatedRoute: ActivatedRoute) {
  }

  deletePartner(partnerId: number): void {
    this.projectPartnerService.deleteProjectPartner(partnerId, this.projectId)
      .pipe(
        take(1),
        tap(() => this.newPageIndex$.next(Tables.DEFAULT_INITIAL_PAGE_INDEX)),
        tap(() => Log.info('Deleted partner: ', this, partnerId)),
        tap(() => this.projectApplicationFormSidenavService.refreshPartners(this.projectId)),
      ).subscribe();
  }
}
