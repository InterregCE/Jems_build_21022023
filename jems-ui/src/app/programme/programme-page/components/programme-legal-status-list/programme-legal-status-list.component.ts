import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {ViewEditFormComponent} from '@common/components/forms/view-edit-form.component';
import {AbstractControl, FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {ProgrammeLegalStatusDTO, ProgrammeLegalStatusUpdateDTO} from '@cat/api';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {ProgrammeEditableStateStore} from '../../services/programme-editable-state-store.service';
import {FormState} from '@common/components/forms/form-state';
import {tap} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';


@UntilDestroy()
@Component({
  selector: 'jems-programme-legal-status-list',
  templateUrl: './programme-legal-status-list.component.html',
  styleUrls: ['./programme-legal-status-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgrammeLegalStatusListComponent extends ViewEditFormComponent implements OnInit, OnChanges {
  @Input()
  legalStatuses: ProgrammeLegalStatusDTO[];

  @Output()
  saveLegalStatuses = new EventEmitter<ProgrammeLegalStatusUpdateDTO>();

  isProgrammeSetupRestricted: boolean;
  toDeleteIds: number[] = [];

  statusForm = new FormGroup({});

  constructor(protected changeDetectorRef: ChangeDetectorRef,
              private formBuilder: FormBuilder,
              protected translationService: TranslateService,
              public programmeEditableStateStore: ProgrammeEditableStateStore) {
    super(changeDetectorRef, translationService);
    this.programmeEditableStateStore.isProgrammeEditableDependingOnCall$.pipe(
        tap(isProgrammeEditingLimited => this.isProgrammeSetupRestricted = isProgrammeEditingLimited),
        untilDestroyed(this)
    ).subscribe();
  }

  ngOnInit(): void {
    super.ngOnInit();
    this.statusForm = this.formBuilder.group({
      legalStatuses: this.formBuilder.array([]),
    });
    this.resetForm();
  }

  get legalStatusesForm(): FormArray {
    return this.statusForm.get('legalStatuses') as FormArray;
  }

  getForm(): FormGroup | null {
    return this.statusForm;
  }

  resetForm(): void {
    this.legalStatusesForm.clear();
    this.legalStatuses.forEach(legalStatus => this.addControl(legalStatus));
    this.changeFormState$.next(FormState.VIEW);
  }

  addNewLegalStatus(): void {
    this.addControl();
  }

  addControl(legalStatus?: ProgrammeLegalStatusDTO): void {
    this.legalStatusesForm.push(this.formBuilder.group({
      id: [legalStatus?.id],
      type: [legalStatus?.type || ProgrammeLegalStatusDTO.TypeEnum.OTHER ],
      description: [legalStatus?.description || []],
    }));
  }

  deleteLegalStatus(elementIndex: number, elementId?: number): void {
    this.legalStatusesForm.removeAt(elementIndex);
    if (elementId) {
      this.toDeleteIds.push(elementId);
    }
  }

  onSubmit(): void {
    this.saveLegalStatuses.emit({
      toPersist: this.statusForm.controls.legalStatuses.value
        .map((ls: any) => ({
            id: ls.id,
            description: ls.description,
            type: ls.type,
          })
        ),
      toDeleteIds: this.toDeleteIds
    });
  }

  isPublicOrPrivate(formGroup: AbstractControl): boolean {
    return formGroup.get('type')?.value === ProgrammeLegalStatusDTO.TypeEnum.PRIVATE || formGroup.get('type')?.value === ProgrammeLegalStatusDTO.TypeEnum.PUBLIC;
  }

  protected enterEditMode(): void {
    this.toDeleteIds = [];
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.legalStatuses && this.legalStatusesForm) {
      this.resetForm();
    }
  }

}
