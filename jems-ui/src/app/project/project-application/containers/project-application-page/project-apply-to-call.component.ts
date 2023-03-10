import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ProjectService} from '@cat/api';
import {Permission} from '../../../../security/permissions/permission';
import {HttpErrorResponse} from '@angular/common/http';
import {catchError, take, tap} from 'rxjs/operators';
import {Log} from '../../../../common/utils/log';
import {Subject} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {LocaleDatePipe} from '../../../../common/pipe/locale-date.pipe';
import {FormBuilder, Validators} from '@angular/forms';
import {RoutingService} from '../../../../common/services/routing.service';
import {APIError} from '../../../../common/models/APIError';
import moment from 'moment/moment';
import {Alert} from '@common/components/forms/alert';
import {ApplyToCallPageStore} from './apply-to-call-page-store.service';

@Component({
  selector: 'jems-project-apply-to-call',
  templateUrl: 'project-apply-to-call.component.html',
  styleUrls: ['project-apply-to-call.component.scss'],
  providers: [ApplyToCallPageStore],
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ProjectApplyToCallComponent {
  Permission = Permission;
  Alert = Alert;

  submissionForm = this.formBuilder.group({
    acronym: ['', [Validators.required, Validators.pattern(/(?!^\s+$)^.*$/m), Validators.maxLength(25)]]
  });
  callId = this.activatedRoute.snapshot.params.callId;
  saveError$ = new Subject<APIError>();

  constructor(public callStore: ApplyToCallPageStore,
              private formBuilder: FormBuilder,
              private router: RoutingService,
              private activatedRoute: ActivatedRoute,
              private localeDatePipe: LocaleDatePipe,
              protected projectService: ProjectService) {
  }

  getFormattedTimeLeft(dateToFormat: Date): { [key: string]: string | number } {
    const endDate = moment(dateToFormat);
    const now = moment(new Date());
    const diff = moment.duration(endDate.diff(now));
    const daysLeft = Math.floor(diff.asDays());

    return {
      date: this.localeDatePipe.transform(dateToFormat),
      days: daysLeft > 0 ? daysLeft : 0,
      hours: diff.hours() > 0 ? diff.hours() : 0,
      minutes: diff.minutes() > 0 ? diff.minutes() : 0
    };
  }

  createApplication(): void {
    const project = {
      acronym: this.submissionForm?.controls?.acronym?.value,
      projectCallId: this.callId
    };
    this.projectService.createProject(project)
      .pipe(
        take(1),
        tap(saved => Log.info('Created project application:', this, saved)),
        tap((data) => this.router.navigate(['app/project/detail', data.id, 'applicationFormIdentification'])),
        catchError((error: HttpErrorResponse) => {
          this.saveError$.next(error.error);
          throw error;
        })
      )
      .subscribe();
  }
}
