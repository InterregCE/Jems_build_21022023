import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {ProjectPeriodDTO, WorkPackageActivityDeliverableDTO, WorkPackageActivityDTO} from '@cat/api';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {FormService} from '@common/components/section/form/form.service';
import {FormArray, FormBuilder} from '@angular/forms';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {combineLatest, Observable} from 'rxjs';
import {catchError, map, startWith, take, tap} from 'rxjs/operators';
import {ProjectWorkPackagePageStore} from '../project-work-package-page-store.service';
import {ProjectWorkPackageActivitiesTabConstants} from './project-work-package-activities-tab.constants';
import {APPLICATION_FORM} from '@project/common/application-form-model';
import {ProjectPartnerStore} from '@project/project-application/containers/project-application-form-page/services/project-partner-store.service';
import {ProjectPartner} from '@project/model/ProjectPartner';

@UntilDestroy()
@Component({
  selector: 'app-project-work-package-activities-tab',
  templateUrl: './project-work-package-activities-tab.component.html',
  styleUrls: ['./project-work-package-activities-tab.component.scss'],
  providers: [FormService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectWorkPackageActivitiesTabComponent implements OnInit {
  constants = ProjectWorkPackageActivitiesTabConstants;
  APPLICATION_FORM = APPLICATION_FORM;

  separatorKeysCodes: number[] = [ENTER, COMMA];

  form = this.formBuilder.group({
    activities: this.formBuilder.array([])
  });

  data$: Observable<{
    activities: WorkPackageActivityDTO[],
    periods: ProjectPeriodDTO[],
    partners: ProjectPartner[],
    workPackageNumber: number
  }>;

  constructor(public formService: FormService,
              private formBuilder: FormBuilder,
              private partnerStore: ProjectPartnerStore,
              private workPackageStore: ProjectWorkPackagePageStore) {
    this.formService.init(this.form, this.workPackageStore.isProjectEditable$);
  }

  ngOnInit(): void {
    combineLatest([
      this.workPackageStore.activities$, this.formService.reset$.pipe(startWith(null))
    ])
      .pipe(
        map(([activities]) => this.resetForm(activities)),
        untilDestroyed(this)
      ).subscribe();

    this.data$ = combineLatest([
      this.workPackageStore.activities$,
      this.workPackageStore.workPackage$,
      this.workPackageStore.projectForm$,
      this.partnerStore.partners$,
    ]).pipe(
      map(([activities, workPackage, projectForm, partners]) => ({
          activities,
          periods: projectForm.periods,
          workPackageNumber: workPackage.number,
          partners,
        })
      ));
  }

  updateActivities(): void {
    const values = (this.activities.value as any[]).map(activity => ({...activity, partnerIds: activity.partnerIds.map((partner: any) => partner.id)}));
    this.workPackageStore.saveWorkPackageActivities(values)
      .pipe(
        take(1),
        tap(() => this.formService.setSuccess('project.work.package.tab.activities.saved')),
        catchError(err => this.formService.setError(err))
      ).subscribe();
  }

  addNewActivity(): void {
    this.addActivity();
    this.formService.setDirty(true);
  }

  removeActivity(index: number): void {
    this.activities.removeAt(index);
    this.formService.setDirty(true);
  }

  get activities(): FormArray {
    return this.form.get(this.constants.ACTIVITIES.name) as FormArray;
  }

  addNewDeliverable(activityIndex: number): void {
    this.addDeliverable(activityIndex);
    this.formService.setDirty(true);
  }

  deliverables(activityIndex: number): FormArray {
    return this.activities.at(activityIndex).get(this.constants.DELIVERABLES.name) as FormArray;
  }

  partners(activityIndex: number): FormArray {
    return this.activities.at(activityIndex).get(this.constants.PARTNERS.name) as FormArray;
  }

  removeDeliverable(activityIndex: number, deliverableIndex: number): void {
    this.deliverables(activityIndex).removeAt(deliverableIndex);
    this.formService.setDirty(true);
  }

  addActivityVisible(): boolean {
    return this.form.enabled && this.activities.length < 20;
  }

  addDeliverableVisible(activityIndex: number): boolean {
    return this.form.enabled && this.deliverables(activityIndex).length < 20;
  }

  addPartner(activityIndex: number, partnerId: number): void {
    const values = this.getCurrentlySelectedPartnerIds(activityIndex);
    values.push(partnerId);
    values.sort((a, b) => a - b);

    this.partners(activityIndex).insert(values.indexOf(partnerId), this.formBuilder.group({
      id: partnerId,
    }));
    this.formService.setDirty(true);
  }

  getCurrentlySelectedPartnerIds(activityIndex: number): number[] {
    return (this.partners(activityIndex).value as { id: number }[]).map(x => x.id);
  }

  removePartner(activityIndex: number, index: number): void {
    this.partners(activityIndex).removeAt(index);
    this.formService.setDirty(true);
  }

  getAbbreviationForPartnerId(partners: ProjectPartner[], partnerId: number): string {
    return partners.find(partner => partner.id === partnerId)?.abbreviation || '';
  }

  getPartnersWithoutSelected(partners: ProjectPartner[], activityIndex: number): ProjectPartner[] {
    return partners.filter(partner => !this.getCurrentlySelectedPartnerIds(activityIndex).includes(partner.id));
  }

  private resetForm(activities: WorkPackageActivityDTO[]): void {
    this.activities.clear();
    activities.forEach((activity, index) => {
      this.addActivity(activity);
      activity.deliverables?.forEach(deliverable => this.addDeliverable(index, deliverable));
      activity.partnerIds?.forEach(partnerId => this.addPartner(index, partnerId));
    });
    this.formService.resetEditable();
    this.formService.setDirty(false);
  }

  private addActivity(existing?: WorkPackageActivityDTO): void {
    this.activities.push(this.formBuilder.group(
      {
        title: this.formBuilder.control(existing?.title || [], this.constants.TITLE.validators),
        description: this.formBuilder.control(existing?.description || []),
        startPeriod: this.formBuilder.control(existing?.startPeriod || ''),
        endPeriod: this.formBuilder.control(existing?.endPeriod || ''),
        deliverables: this.formBuilder.array([]),
        partnerIds: this.formBuilder.array([]),
      },
      {
        validators: this.constants.PERIODS.validators
      })
    );
  }

  private addDeliverable(activityIndex: number, existing?: WorkPackageActivityDeliverableDTO): void {
    this.deliverables(activityIndex).push(this.formBuilder.group({
      description: this.formBuilder.control(existing?.description || [], this.constants.DELIVERABLE.validators),
      period: this.formBuilder.control(existing?.period || ''),
    }));
  }

}
