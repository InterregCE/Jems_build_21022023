import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output
} from '@angular/core';
import {ViewEditForm} from '@common/components/forms/view-edit-form';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {filter, take, takeUntil} from 'rxjs/operators';
import {FormState} from '@common/components/forms/form-state';
import {Forms} from '../../../../common/utils/forms';
import { Permission } from 'src/app/security/permissions/permission';
import {
  ProgrammeLumpSumDTO
} from '@cat/api';
import {SelectionModel} from '@angular/cdk/collections';

@Component({
  selector: 'app-programme-lump-sum-detail',
  templateUrl: './programme-lump-sum-detail.component.html',
  styleUrls: ['./programme-lump-sum-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgrammeLumpSumDetailComponent extends ViewEditForm implements OnInit {

  Permission = Permission;
  ProgrammeLumpSumDTO = ProgrammeLumpSumDTO;

  @Input()
  lumpSum: ProgrammeLumpSumDTO;
  @Input()
  isCreate: boolean;
  @Output()
  createLumpSum: EventEmitter<ProgrammeLumpSumDTO> = new EventEmitter<ProgrammeLumpSumDTO>();
  @Output()
  updateLumpSum: EventEmitter<ProgrammeLumpSumDTO> = new EventEmitter<ProgrammeLumpSumDTO>();
  @Output()
  cancelCreate: EventEmitter<void> = new EventEmitter<void>();

  lumpSumForm = this.formBuilder.group({
    name: ['', Validators.compose([Validators.required, Validators.maxLength(50)])],
    description: ['', Validators.maxLength(500)],
    cost: ['', Validators.required],
    allowSplitting: ['', Validators.required],
    phase: ['', Validators.required],
    categories: ['', Validators.required]
  });

  nameErrors = {
    required: 'lump.sum.name.should.not.be.empty',
    maxlength: 'lump.sum.name.size.too.long',
  };

  descriptionErrors = {
    maxlength: 'lump.sum.description.size.too.long',
  };

  costErrors = {
    required: 'lump.sum.cost.should.not.be.empty',
  };

  allowSplittingErrors = {
    required: 'lump.sum.splitting.should.not.be.empty'
  };

  phaseErrors = {
    required: 'lump.sum.phase.should.not.be.empty'
  };

  categoriesErrors = {
    required: 'lump.sum.categories.should.not.be.empty'
  };

  previousSplitting = '';
  previousPhase = '';
  selection = new SelectionModel<ProgrammeLumpSumDTO.CategoriesEnum>(true, []);
  categories = [
    ProgrammeLumpSumDTO.CategoriesEnum.StaffCosts,
    ProgrammeLumpSumDTO.CategoriesEnum.OfficeAndAdministrationCosts,
    ProgrammeLumpSumDTO.CategoriesEnum.TravelAndAccommodationCosts,
    ProgrammeLumpSumDTO.CategoriesEnum.ExternalCosts,
    ProgrammeLumpSumDTO.CategoriesEnum.EquipmentCosts,
    ProgrammeLumpSumDTO.CategoriesEnum.InfrastructureCosts
  ];
  validNumberOfSelections = false;

  constructor(private formBuilder: FormBuilder,
              private dialog: MatDialog,
              protected changeDetectorRef: ChangeDetectorRef) {
    super(changeDetectorRef);
  }

  ngOnInit(): void {
    super.ngOnInit();
    if (this.isCreate) {
      this.changeFormState$.next(FormState.EDIT);
      this.selection.clear();
    } else {
      this.lumpSumForm.controls.name.setValue(this.lumpSum.name);
      this.lumpSumForm.controls.description.setValue(this.lumpSum.description);
      this.lumpSumForm.controls.cost.setValue(this.lumpSum.cost);
      this.previousSplitting = this.lumpSum.splittingAllowed ? 'Yes' : 'No';
      this.previousPhase = this.lumpSum.phase;
      this.selection.clear();
      this.lumpSum.categories.forEach(category => {
        this.selection.select(category);
      });
      this.validNumberOfSelections = this.selection.selected.length >= 2;
      this.changeFormState$.next(FormState.VIEW);
    }
  }

  getForm(): FormGroup | null {
    return this.lumpSumForm;
  }

  onSubmit(): void {
    Forms.confirmDialog(
      this.dialog,
      this.isCreate ? 'lump.sum.final.dialog.title.save' : 'lump.sum.final.dialog.title.update',
      this.isCreate ? 'lump.sum.final.dialog.message.save' : 'lump.sum.final.dialog.message.update'
    ).pipe(
      take(1),
      takeUntil(this.destroyed$),
      filter(yes => !!yes)
    ).subscribe(() => {
      if (this.isCreate) {
        this.createLumpSum.emit({
          name: this.lumpSumForm?.controls?.name?.value,
          description: this.lumpSumForm?.controls?.description?.value,
          cost: this.lumpSumForm?.controls?.cost?.value,
          splittingAllowed: this.previousSplitting === 'Yes',
          phase: this.getCorrectPhase(this.previousPhase),
          categories: this.selection.selected
        } as ProgrammeLumpSumDTO);
      } else {
        this.updateLumpSum.emit({
          id: this.lumpSum?.id,
          name: this.lumpSumForm?.controls?.name?.value,
          description: this.lumpSumForm?.controls?.description?.value,
          cost: this.lumpSumForm?.controls?.cost?.value,
          splittingAllowed: this.previousSplitting === 'Yes',
          phase: this.getCorrectPhase(this.previousPhase),
          categories: this.selection.selected
        });
      }
    });
  }

  onCancel(): void {
    if (this.isCreate) {
      this.cancelCreate.emit();
    } else {
      this.changeFormState$.next(FormState.VIEW);
    }
  }

  changeSplitting(value: string): void {
    this.lumpSumForm.controls.allowSplitting.setValue(value);
    this.previousSplitting = value;
  }

  changePhase(value: string): void {
    this.lumpSumForm.controls.phase.setValue(value);
    this.previousPhase = value;
  }

  getCorrectPhase(value: string): ProgrammeLumpSumDTO.PhaseEnum {
    if (value === 'Preparation') {
      return ProgrammeLumpSumDTO.PhaseEnum.Preparation;
    }

    if (value === 'implementation') {
      return ProgrammeLumpSumDTO.PhaseEnum.Implementation;
    }
    return ProgrammeLumpSumDTO.PhaseEnum.Closure;
  }

  checkSelection(element: ProgrammeLumpSumDTO.CategoriesEnum): void {
    this.selection.toggle(element);
    this.validNumberOfSelections = this.selection.selected.length >= 2;
    if (this.validNumberOfSelections) {
      this.lumpSumForm.controls.categories.setValue(true);
    } else {
      this.lumpSumForm.controls.categories.setValue(null);
    }
  }

  protected enterEditMode(): void {
    if (this.lumpSum) {
      this.lumpSumForm.controls.allowSplitting.setErrors(null);
      this.lumpSumForm.controls.phase.setErrors(null);
      this.lumpSumForm.controls.categories.setErrors(null);
    }
  }
}