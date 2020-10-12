import {Injectable} from '@angular/core';
import {combineLatest, merge, Observable, of, ReplaySubject, Subject} from 'rxjs';
import {
  InputProjectEligibilityAssessment,
  InputProjectQualityAssessment,
  InputProjectStatus,
  InputRevertProjectStatus,
  OutputProject,
  OutputProjectStatus,
  OutputRevertProjectStatus,
  ProjectService,
  ProjectStatusService
} from '@cat/api';
import {catchError, flatMap, shareReplay, startWith, tap, withLatestFrom} from 'rxjs/operators';
import {Log} from '../../../../../common/utils/log';
import {Router} from '@angular/router';
import {PermissionService} from '../../../../../security/permissions/permission.service';
import {Permission} from '../../../../../security/permissions/permission';
import {I18nValidationError} from '@common/validation/i18n-validation-error';
import {HttpErrorResponse} from '@angular/common/http';

/**
 * Stores project related information.
 */
@Injectable()
export class ProjectStore {
  private projectId$ = new ReplaySubject<number>(1);
  private projectAcronym$ = new ReplaySubject<string>(1);
  private newStatus$ = new Subject<InputProjectStatus>();
  private newEligibilityAssessment$ = new Subject<InputProjectEligibilityAssessment>();
  private newQualityAssessment$ = new Subject<InputProjectQualityAssessment>();
  private newRevertProjectStatus$ = new Subject<InputRevertProjectStatus>();
  private revertStatusChanged$ = new Subject<void>();
  private changeStatusError$ = new Subject<I18nValidationError | null>();

  private projectById$ = this.projectId$
    .pipe(
      flatMap(id => this.projectService.getProjectById(id)),
      tap(project => Log.info('Fetched project:', this, project))
    );

  private changedStatus$ = this.newStatus$
    .pipe(
      withLatestFrom(this.projectId$),
      flatMap(([newStatus, id]) =>
        this.projectStatusService.setProjectStatus(id, newStatus)),
      tap(saved => this.projectStatus$.next(saved.projectStatus.status)),
      tap(saved => Log.info('Updated project status status:', this, saved)),
      tap(saved => this.router.navigate(['app', 'project', 'detail', saved.id])),
      catchError((error: HttpErrorResponse) => {
        this.changeStatusError$.next(error.error);
        throw error;
      })
    );

  private changedEligibilityAssessment$ = this.newEligibilityAssessment$
    .pipe(
      withLatestFrom(this.projectId$),
      flatMap(([assessment, id]) => this.projectStatusService.setEligibilityAssessment(id, assessment)),
      tap(saved => Log.info('Updated project eligibility assessment:', this, saved)),
      tap(saved => this.router.navigate(['app', 'project', 'detail', saved.id]))
    );

  private changedQualityAssessment$ = this.newQualityAssessment$
    .pipe(
      withLatestFrom(this.projectId$),
      flatMap(([assessment, id]) => this.projectStatusService.setQualityAssessment(id, assessment)),
      tap(saved => Log.info('Updated project quality assessment:', this, saved)),
      tap(saved => this.router.navigate(['app', 'project', 'detail', saved.id]))
    )

  private revertedProjectStatus$ = this.newRevertProjectStatus$
    .pipe(
      withLatestFrom(this.projectId$),
      flatMap(([status, id]) => this.projectStatusService.revertLastDecision(id, status)),
      tap(() => this.revertStatusChanged$.next()),
      tap(saved => Log.info('Reverted project status:', this, saved))
    );

  private revertStatus$ = combineLatest([
    this.permissionService.permissionsChanged(),
    this.projectId$,
    this.revertStatusChanged$.pipe(startWith(null))
  ])
    .pipe(
      flatMap(([permissions, id]) => permissions[0] === Permission.ADMINISTRATOR
        ? this.projectStatusService.findPossibleDecisionRevertStatus(id)
        : of(null)
      ),
      tap(revertStatus => Log.info('Fetched the project revert status', revertStatus))
    );

  private projectStatus$ = new ReplaySubject<OutputProjectStatus.StatusEnum>(1);
  private project$ =
    merge(
      this.projectById$,
      this.changedStatus$,
      this.changedEligibilityAssessment$,
      this.changedQualityAssessment$,
      this.revertedProjectStatus$
    )
      .pipe(
        tap(project => this.projectAcronym$.next(project.acronym)),
        shareReplay(1)
      );


  constructor(private projectService: ProjectService,
              private projectStatusService: ProjectStatusService,
              private router: Router,
              private permissionService: PermissionService) {
  }

  init(projectId: number) {
    this.projectId$.next(projectId);
  }

  getProject(): Observable<OutputProject> {
    return this.project$;
  }

  getStatus(): Observable<OutputProjectStatus.StatusEnum> {
    return this.projectStatus$.asObservable();
  }

  changeStatus(newStatus: InputProjectStatus) {
    this.newStatus$.next(newStatus)
  }

  getChangeStatusError(): Observable<I18nValidationError | null> {
    return this.changeStatusError$.asObservable();
  }

  setEligibilityAssessment(assessment: InputProjectEligibilityAssessment): void {
    this.newEligibilityAssessment$.next(assessment);
  }

  setQualityAssessment(assessment: InputProjectQualityAssessment): void {
    this.newQualityAssessment$.next(assessment);
  }

  getRevertStatus(): Observable<OutputRevertProjectStatus | null> {
    return this.revertStatus$;
  }

  revertStatus(status: InputRevertProjectStatus): void {
    this.newRevertProjectStatus$.next(status);
  }

  getAcronym(): Observable<string> {
    return this.projectAcronym$.asObservable();
  }
}