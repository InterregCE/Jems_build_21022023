import {ChangeDetectionStrategy, ChangeDetectorRef, Component} from '@angular/core';
import {ProgrammePriorityDetailPageStore} from './programme-priority-detail-page-store.service';
import {ProgrammePageSidenavService} from '../../../programme-page/services/programme-page-sidenav.service';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of} from 'rxjs';
import {ProgrammePriorityDTO, ProgrammeSpecificObjectiveDTO} from '@cat/api';
import {catchError, filter, map, tap} from 'rxjs/operators';
import {FormArray, FormBuilder} from '@angular/forms';
import {ProgrammePriorityDetailPageConstants} from './programme-priority-detail-page.constants';
import {take} from 'rxjs/internal/operators';
import {Alert} from '@common/components/forms/alert';
import {Forms} from '../../../../common/utils/forms';
import {MatDialog} from '@angular/material/dialog';
import {I18nValidationError} from '@common/validation/i18n-validation-error';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-programme-priority-detail-page',
  templateUrl: './programme-priority-detail-page.component.html',
  styleUrls: ['./programme-priority-detail-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ProgrammePriorityDetailPageStore]
})
export class ProgrammePriorityDetailPageComponent {
  Alert = Alert;
  constants = ProgrammePriorityDetailPageConstants;
  priorityId = this.activatedRoute.snapshot.params.priorityId;
  objectivePoliciesAlreadyInUse: string[] = [];

  data$: Observable<{
    priority: ProgrammePriorityDTO | {},
    objectives: string[],
    freePrioritiesWithPolicies: { [key: string]: Array<string>; },
  }>;

  form = this.formBuilder.group({
    code: this.formBuilder.control('', this.constants.CODE.validators),
    title: this.formBuilder.control('', this.constants.TITLE.validators),
    objective: this.formBuilder.control('', this.constants.OBJECTIVE.validators),
    specificObjectives: this.formBuilder.array([], {validators: this.constants.mustHaveSpecificObjectiveSelected})
  });

  // TODO: remove when new edit mode is introduced
  saveSuccess: string;
  saveError: I18nValidationError;

  constructor(private programmePageSidenavService: ProgrammePageSidenavService,
              private activatedRoute: ActivatedRoute,
              private formBuilder: FormBuilder,
              private changeDetectorRef: ChangeDetectorRef, // TODO: remove when new edit mode is introduced
              private dialog: MatDialog,
              public pageStore: ProgrammePriorityDetailPageStore) {

    this.data$ = combineLatest([
      this.pageStore.priority$,
      this.pageStore.policies$,
    ]).pipe(
      tap(([priority, setup]) =>
        this.objectivePoliciesAlreadyInUse = setup.objectivePoliciesAlreadyInUse as string[]
      ),
      map(([priority, setup]) => ({
          priority,
          objectives: this.getAvailableObjectives((priority as any).objective, setup.freePrioritiesWithPolicies),
          freePrioritiesWithPolicies: setup.freePrioritiesWithPolicies,
          objectivePoliciesAlreadyInUse: setup.objectivePoliciesAlreadyInUse as string[]
        })
      ),
      tap(data => this.resetForm(data.priority as ProgrammePriorityDTO, data.freePrioritiesWithPolicies)),
      tap(data => (data.priority as any)?.id ? this.form.disable() : this.form.enable())
    );
  }

  save(): void {
    const priority: ProgrammePriorityDTO = this.form.value;
    priority.objective = this.form.get(this.constants.OBJECTIVE.name)?.value;
    priority.specificObjectives = this.specificObjectives.controls
      .filter(control => !!control.get(this.constants.POLICY_SELECTED.name)?.value)
      .map(control => control.value);

    if (!this.priorityId) {
      this.pageStore.createPriority(priority)
        .pipe(
          take(1),
          tap(() => this.programmePageSidenavService.goToPriorities()),
          catchError(err => this.handleError(err, priority))
        ).subscribe();
    } else {
      this.saveSuccess = '';
      this.pageStore.updatePriority(this.priorityId, priority)
        .pipe(
          take(1),
          tap(() => this.handleSuccess()),
          catchError(err => this.handleError(err, priority))
        ).subscribe();
    }
  }

  resetForm(priority: ProgrammePriorityDTO,
            freePrioritiesWithPolicies: { [key: string]: Array<string>; }): void {
    this.form.patchValue(priority);
    this.changeCurrentObjective(priority?.objective, freePrioritiesWithPolicies, priority.specificObjectives);
  }

  cancel(priority: ProgrammePriorityDTO,
         freePrioritiesWithPolicies: { [key: string]: Array<string>; }): void {
    if (!this.priorityId) {
      this.programmePageSidenavService.goToPriorities();
      return;
    }
    this.resetForm(priority, freePrioritiesWithPolicies);
    this.form.disable(); // TODO: remove when new edit mode is introduced
  }

  submit(): void {
    if (this.priorityId) {
      this.save();
      return;
    }

    // TODO: probably remove when new edit mode is introduced?
    Forms.confirmDialog(
      this.dialog,
      'programme.priority.dialog.title',
      'programme.priority.dialog.message'
    ).pipe(
      take(1),
      filter(yes => !!yes)
    ).subscribe(() => this.save());
  }

  changeCurrentObjective(objective: ProgrammePriorityDTO.ObjectiveEnum,
                         freePrioritiesWithPolicies: { [p: string]: Array<string> },
                         selectedSpecificObjectives?: ProgrammeSpecificObjectiveDTO[]): void {
    this.specificObjectives.clear();
    selectedSpecificObjectives?.forEach(
      selected => this.addSpecificObjective(selected.programmeObjectivePolicy, true, selected.code)
    );
    const freePolicies = freePrioritiesWithPolicies[objective];
    freePolicies?.forEach(policy => this.addSpecificObjective(policy, false, ''));
  }

  setCheckedStatus(specificObjectiveIndex: number, checked: boolean): void {
    this.specificObjectives.at(specificObjectiveIndex).get(this.constants.POLICY_SELECTED.name)?.patchValue(checked);
  }

  specificObjectiveError(): { [key: string]: any } | null {
    if (!this.saveError?.i18nFieldErrors?.specificObjectives) {
      return null;
    }
    return {
      i18nKey: this.saveError?.i18nFieldErrors?.specificObjectives.i18nKey,
      i8nArguments: {
        arg: this.saveError?.i18nFieldErrors?.specificObjectives?.i18nArguments?.join(',')
      }
    };
  }

  private addSpecificObjective(policy: string, selected: boolean, code: string): void {
    const control = this.formBuilder.group(
      {
        selected: this.formBuilder.control(selected),
        code: this.formBuilder.control(code, this.constants.POLICY_CODE.validators),
        programmeObjectivePolicy: this.formBuilder.control(policy),
      },
      {
        validators: this.constants.selectedSpecificObjectiveCodeRequired
      });
    if (this.objectivePoliciesAlreadyInUse.find(used => used === policy)) {
      control.disable();
      this.form.get(this.constants.OBJECTIVE.name)?.disable();
    }
    this.specificObjectives.push(control);
  }

  // TODO: remove when new edit mode is introduced
  private handleSuccess(): void {
    this.saveSuccess = 'programme.priority.save.success';
    this.changeDetectorRef.markForCheck();
    setTimeout(() => {
        this.saveSuccess = '';
        this.changeDetectorRef.markForCheck();
      },
               4000);
  }

  // TODO: remove when new edit mode is introduced
  private handleError(err: HttpErrorResponse, priority: ProgrammePriorityDTO): Observable<ProgrammePriorityDTO> {
    this.saveError = err.error;
    this.changeDetectorRef.markForCheck();
    return of(priority);
  }

  private getAvailableObjectives(currentObjective: string, freeObjectives: { [key: string]: Array<string>; }): string[] {
    const objectives = Object.keys(freeObjectives);
    if (!currentObjective || objectives.find(obj => obj === currentObjective)) {
      return objectives;
    }
    return [currentObjective, ...objectives];
  }

  get specificObjectives(): FormArray {
    return this.form.get(this.constants.SPECIFIC_OBJECTIVES.name) as FormArray;
  }
}