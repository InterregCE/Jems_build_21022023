import {ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output} from '@angular/core';
import {ViewEditFormComponent} from '@common/components/forms/view-edit-form.component';
import {InputProgrammeStrategy, OutputProgrammeStrategy} from '@cat/api';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {Permission} from '../../../../security/permissions/permission';
import {Forms} from '../../../../common/utils/forms';
import {filter, take, takeUntil} from 'rxjs/operators';
import {FormState} from '@common/components/forms/form-state';
import {SelectionModel} from '@angular/cdk/collections';
import {UntilDestroy} from '@ngneat/until-destroy';
import {ProgrammeEditableStateStore} from '../../services/programme-editable-state-store.service';
import {TranslateService} from '@ngx-translate/core';

@UntilDestroy()
@Component({
  selector: 'jems-programme-strategies',
  templateUrl: './programme-strategies.component.html',
  styleUrls: ['./programme-strategies.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgrammeStrategiesComponent extends ViewEditFormComponent {

  Permission = Permission;
  @Input()
  strategies: OutputProgrammeStrategy[];
  @Output()
  updateProgrammeStrategy: EventEmitter<InputProgrammeStrategy[]> = new EventEmitter<InputProgrammeStrategy[]>();

  initialSelection = new SelectionModel<OutputProgrammeStrategy>(true, []);
  selection = new SelectionModel<OutputProgrammeStrategy>(true, []);
  programmeStrategyForm = this.formBuilder.group({});


  constructor(private formBuilder: FormBuilder,
              private dialog: MatDialog,
              public programmeEditableStateStore: ProgrammeEditableStateStore,
              protected translationService: TranslateService,
              protected changeDetectorRef: ChangeDetectorRef) {
    super(changeDetectorRef, translationService);
  }

  getForm(): FormGroup | null {
    return this.programmeStrategyForm;
  }

  onSubmit(): void {
    Forms.confirmDialog(
      this.dialog,
      'programme.strategy.dialog.title.save',
      'programme.strategy.dialog.message.save'
    ).pipe(
      take(1),
      takeUntil(this.destroyed$),
      filter(yes => !!yes)
    ).subscribe(() => {
        this.updateProgrammeStrategy.emit(this.buildSaveEntity());
    });
  }

  onCancel(): void {
    this.changeFormState$.next(FormState.VIEW);
  }

  private buildSaveEntity(): InputProgrammeStrategy[] {
    return this.strategies
      .map(strategy => ({
        strategy: strategy.strategy,
        active: this.selection.isSelected(strategy),
      } as InputProgrammeStrategy));
  }

  protected enterViewMode(): void {
    this.initialSelection.clear();
    this.initialSelection.select(...this.strategies.filter(element => element.active));
    this.selection.select(...this.strategies.filter(element => element.active));
    this.selection.deselect(...this.strategies.filter(element => !element.active));
    this.changeDetectorRef.markForCheck();
  }

}
