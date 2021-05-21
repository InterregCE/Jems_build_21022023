import {Injectable} from '@angular/core';
import {
  InputProjectAssociatedOrganizationCreate,
  InputProjectAssociatedOrganizationUpdate,
  OutputProjectAssociatedOrganizationDetail,
  ProjectAssociatedOrganizationService,
} from '@cat/api';
import {combineLatest, merge, Observable, of, ReplaySubject, Subject} from 'rxjs';
import {
  catchError,
  distinctUntilChanged,
  map,
  mergeMap,
  shareReplay,
  switchMap,
  tap,
  withLatestFrom
} from 'rxjs/operators';
import {Log} from '../../../../../common/utils/log';
import {ProjectStore} from '../../project-application-detail/services/project-store.service';
import {HttpErrorResponse} from '@angular/common/http';
import {Router} from '@angular/router';
import {ProjectVersionStore} from '../../../../services/project-version-store.service';

@Injectable()
export class ProjectAssociatedOrganizationStore {

  private associatedOrganizationId$ = new ReplaySubject<number | null>(1);
  private projectId$ = this.projectStore.getProject()
    .pipe(
      map(project => project.id),
      shareReplay(1)
    );

  saveAssociatedOrganization$ = new Subject<InputProjectAssociatedOrganizationUpdate>();
  createAssociatedOrganization$ = new Subject<InputProjectAssociatedOrganizationCreate>();
  associatedOrganizationSaveSuccess$ = new Subject<boolean>();
  associatedOrganizationSaveError$ = new Subject<HttpErrorResponse | null>();

  private associatedOrganizationById$ = combineLatest([
    this.associatedOrganizationId$,
    this.projectId$,
    this.projectVersionStore.currentRouteVersion$
  ])
    .pipe(
      distinctUntilChanged(),
      mergeMap(([associatedOrganizationId, projectId, version]) => associatedOrganizationId
        ? this.associatedOrganizationService.getAssociatedOrganizationById(associatedOrganizationId, projectId, version)
        : of({})
      ),
      tap(projectAssociatedOrganization => Log.info('Fetched project associatedOrganization:', this, projectAssociatedOrganization)),
    );


  private savedAssociatedOrganization$ = this.saveAssociatedOrganization$
    .pipe(
      withLatestFrom(this.projectId$),
      switchMap(([associatedOrganizationUpdate, projectId]) =>
        this.associatedOrganizationService.updateAssociatedOrganization(projectId, associatedOrganizationUpdate)
          .pipe(
            catchError((error: HttpErrorResponse) => {
              this.associatedOrganizationSaveError$.next(error);
              return of();
            })
          )
      ),
      tap(() => this.associatedOrganizationSaveError$.next(null)),
      tap(() => this.associatedOrganizationSaveSuccess$.next(true)),
      tap(saved => Log.info('Updated associatedOrganization:', this, saved))
    );

  private createdAssociatedOrganization$ = this.createAssociatedOrganization$
    .pipe(
      withLatestFrom(this.projectId$),
      switchMap(([associatedOrganizationCreate, projectId]) =>
        this.associatedOrganizationService.createAssociatedOrganization(projectId, associatedOrganizationCreate)
          .pipe(
            map(created => ({projectId, associatedOrganization: created})),
            catchError((error: HttpErrorResponse) => {
              this.associatedOrganizationSaveError$.next(error.error);
              return of();
            })
          )
      ),
      tap(saved => Log.info('Created associatedOrganization:', this, saved)),
      tap((created: any) => this.router.navigate([
        'app', 'project', 'detail', created.projectId, 'applicationFormAssociatedOrganization', 'detail', created.associatedOrganization.id
      ])),
    );

  private associatedOrganization$ = merge(
    this.associatedOrganizationById$,
    this.savedAssociatedOrganization$,
    this.createdAssociatedOrganization$,
  )
    .pipe(
      shareReplay(1)
    );

  constructor(private associatedOrganizationService: ProjectAssociatedOrganizationService,
              private projectStore: ProjectStore,
              private projectVersionStore: ProjectVersionStore,
              private router: Router) {
  }

  init(associatedOrganizationId: number | string | null): void {
    this.associatedOrganizationId$.next(Number(associatedOrganizationId));
  }

  getProjectAssociatedOrganization(): Observable<OutputProjectAssociatedOrganizationDetail | any> {
    return this.associatedOrganization$;
  }
}
