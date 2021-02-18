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
import {FormState} from '@common/components/forms/form-state';
import {Forms} from '../../../../common/utils/forms';
import {filter, take, takeUntil} from 'rxjs/operators';
import {Permission} from '../../../../security/permissions/permission';
import {
  ProgrammeUnitCostDTO
} from '@cat/api';
import {SelectionModel} from '@angular/cdk/collections';
import {NumberService} from '../../../../common/services/number.service';

@Component({
  selector: 'app-programme-unit-cost-detail',
  templateUrl: './programme-unit-cost-detail.component.html',
  styleUrls: ['./programme-unit-cost-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgrammeUnitCostDetailComponent extends ViewEditForm implements OnInit {

  Permission = Permission;

  @Input()
  unitCost: ProgrammeUnitCostDTO;
  @Input()
  isCreate: boolean;
  @Output()
  createUnitCost: EventEmitter<ProgrammeUnitCostDTO> = new EventEmitter<ProgrammeUnitCostDTO>();
  @Output()
  updateUnitCost: EventEmitter<ProgrammeUnitCostDTO> = new EventEmitter<ProgrammeUnitCostDTO>();
  @Output()
  cancelCreate: EventEmitter<void> = new EventEmitter<void>();

  unitCostForm = this.formBuilder.group({
    isOneCostCategory: [null, Validators.required],
    name: ['', Validators.compose([Validators.required, Validators.maxLength(50)])],
    description: ['', Validators.maxLength(500)],
    type: ['', Validators.compose([Validators.required, Validators.maxLength(25)])],
    costPerUnit: ['', Validators.required],
    categories: ['', Validators.required]
  });

  nameErrors = {
    required: 'unit.cost.name.should.not.be.empty',
    maxlength: 'unit.cost.name.size.too.long',
  };

  descriptionErrors = {
    maxlength: 'unit.cost.description.size.too.long',
  };

  typeErrors = {
    required: 'unit.cost.type.should.not.be.empty',
    maxlength: 'unit.cost.type.size.too.long',
  };

  costErrors = {
    required: 'programme.unitCost.costPerUnit.invalid',
  };

  categoriesErrorsMultiple = {
    required: 'unit.cost.categories.should.not.be.empty.multiple'
  };

  categoriesErrorsSingle = {
    required: 'unit.cost.categories.should.not.be.empty.single'
  };

  allowMultipleCostCategoriesErrors = {
    required: 'unit.cost.categories.allow.multiple.should.not.be.empty'
  };

  selectionMultiple = new SelectionModel<ProgrammeUnitCostDTO.CategoriesEnum>(true, []);

  selectionSingle = new SelectionModel<ProgrammeUnitCostDTO.CategoriesEnum>(false, []);

  multipleCostCategories = [
    ProgrammeUnitCostDTO.CategoriesEnum.StaffCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.OfficeAndAdministrationCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.TravelAndAccommodationCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.ExternalCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.EquipmentCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.InfrastructureCosts
  ];
  singleCostCategories = [
    ProgrammeUnitCostDTO.CategoriesEnum.StaffCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.TravelAndAccommodationCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.ExternalCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.EquipmentCosts,
    ProgrammeUnitCostDTO.CategoriesEnum.InfrastructureCosts
  ];
  validNumberOfSelections = false;

  constructor(private formBuilder: FormBuilder,
              private dialog: MatDialog,
              protected changeDetectorRef: ChangeDetectorRef,
              public numberService: NumberService) {
    super(changeDetectorRef);
  }

  ngOnInit(): void {
    super.ngOnInit();
    if (this.isCreate) {
      this.changeFormState$.next(FormState.EDIT);
      this.selectionMultiple.clear();
      this.selectionSingle.clear();
    } else {
      this.resetForm();
      this.changeFormState$.next(FormState.VIEW);
    }
  }

  resetForm(): void {
    this.unitCostForm.controls.name.setValue(this.unitCost.name);
    this.unitCostForm.controls.description.setValue(this.unitCost.description);
    this.unitCostForm.controls.type.setValue(this.unitCost.type);
    this.unitCostForm.controls.costPerUnit.setValue(this.unitCost.costPerUnit);
    this.unitCostForm.controls.isOneCostCategory.setValue(this.unitCost.isOneCostCategory);
    this.selectionMultiple.clear();
    this.selectionSingle.clear();
    if (this.unitCost.isOneCostCategory) {
      this.selectionSingle.select(this.unitCost.categories[0]);
      this.validNumberOfSelections = this.selectionMultiple.selected.length === 1;
    } else {
      this.unitCost.categories.forEach(category => {
        this.selectionMultiple.select(category);
        this.validNumberOfSelections = this.selectionMultiple.selected.length >= 2;
      });
    }
  }

  getForm(): FormGroup | null {
    return this.unitCostForm;
  }

  onSubmit(): void {
    Forms.confirmDialog(
      this.dialog,
      this.isCreate ? 'unit.cost.final.dialog.title.save' : 'unit.cost.final.dialog.title.update',
      this.isCreate ? 'unit.cost.final.dialog.message.save' : 'unit.cost.final.dialog.message.update'
    ).pipe(
      take(1),
      takeUntil(this.destroyed$),
      filter(yes => !!yes)
    ).subscribe(() => {
      if (this.isCreate) {
        this.createUnitCost.emit({
          name: this.unitCostForm?.controls?.name?.value,
          description: this.unitCostForm?.controls?.description?.value,
          type: this.unitCostForm?.controls?.type?.value,
          costPerUnit: this.unitCostForm?.controls?.costPerUnit?.value,
          isOneCostCategory: this.unitCostForm?.controls?.isOneCostCategory?.value,
          categories: this.unitCostForm?.controls?.isOneCostCategory?.value ? this.selectionSingle.selected : this.selectionMultiple.selected
        } as ProgrammeUnitCostDTO);
      } else {
        this.updateUnitCost.emit({
          id: this.unitCost?.id,
          name: this.unitCostForm?.controls?.name?.value,
          description: this.unitCostForm?.controls?.description?.value,
          type: this.unitCostForm?.controls?.type?.value,
          costPerUnit: this.unitCostForm?.controls?.costPerUnit?.value,
          isOneCostCategory: this.unitCostForm?.controls?.isOneCostCategory?.value,
          categories: this.unitCostForm?.controls?.isOneCostCategory?.value ? this.selectionSingle.selected : this.selectionMultiple.selected
        });
      }
    });
  }

  onCancel(): void {
    if (this.isCreate) {
      this.cancelCreate.emit();
    } else {
      this.resetForm();
      this.changeFormState$.next(FormState.VIEW);
    }
  }

  checkSelectionMultiple(element: ProgrammeUnitCostDTO.CategoriesEnum): void {
    this.selectionMultiple.toggle(element);
    this.validNumberOfSelections = this.selectionMultiple.selected.length >= 2;
    if (this.validNumberOfSelections) {
      this.unitCostForm.controls.categories.setValue(true);
    } else {
      this.unitCostForm.controls.categories.setValue(null);
    }
  }

  checkSelectionSingle(element: ProgrammeUnitCostDTO.CategoriesEnum): void {
    this.selectionSingle.toggle(element);
    this.validNumberOfSelections = this.selectionSingle.selected.length === 1;
    if (this.validNumberOfSelections) {
      this.unitCostForm.controls.categories.setValue(true);
    } else {
      this.unitCostForm.controls.categories.setValue(null);
    }
  }

  changeAllowedCategories(value: boolean): void {
    this.unitCostForm.controls.isOneCostCategory.setValue(value);
    this.selectionMultiple.clear();
    this.selectionSingle.clear();
    this.validNumberOfSelections = false;
    this.unitCostForm.controls.categories.setValue(null);
  }

  protected enterEditMode(): void {
    if (this.unitCost) {
      this.unitCostForm.controls.categories.setErrors(null);
    }
    if ((this.unitCost.isOneCostCategory && this.unitCost.categories?.length === 1)
        || (!this.unitCost.isOneCostCategory && this.unitCost.categories?.length >= 2)) {
      this.validNumberOfSelections = true;
    }
  }

}
