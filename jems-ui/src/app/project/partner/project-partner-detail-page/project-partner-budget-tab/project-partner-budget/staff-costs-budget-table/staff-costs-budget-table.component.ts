import {ChangeDetectionStrategy, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {
  AbstractControl,
  ControlContainer,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators
} from '@angular/forms';
import {MatTableDataSource} from '@angular/material/table';
import {ProjectPartnerBudgetConstants} from '../project-partner-budget.constants';
import {Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {NumberService} from '../../../../../../common/services/number.service';
import {FormService} from '@common/components/section/form/form.service';
import {StaffCostsBudgetTable} from '../../../../../model/budget/staff-costs-budget-table';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {StaffCostTypeEnum} from '../../../../../model/budget/staff-cost-type.enum';
import {BudgetPeriodDTO, ProjectPeriodDTO} from '@cat/api';
import {Alert} from '@common/components/forms/alert';
import {TableConfig} from '../../../../../../common/directives/table-config/TableConfig';
import {ProgrammeUnitCost} from '../../../../../model/programmeUnitCost';
import {MatSelectChange} from '@angular/material/select/select';

@UntilDestroy()
@Component({
  selector: 'app-staff-costs-budget-table',
  templateUrl: './staff-costs-budget-table.component.html',
  styleUrls: ['./staff-costs-budget-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush

})
export class StaffCostsBudgetTableComponent implements OnInit, OnChanges, OnDestroy {
  Alert = Alert;
  constants = ProjectPartnerBudgetConstants;
  staffCostType = StaffCostTypeEnum;

  @Input()
  editable: boolean;
  @Input()
  staffCostTable: StaffCostsBudgetTable;
  @Input()
  projectPeriods: ProjectPeriodDTO[];
  @Input()
  availableUnitCosts: ProgrammeUnitCost[];

  budgetForm: FormGroup;
  dataSource: MatTableDataSource<AbstractControl>;
  numberOfItems$: Observable<number>;
  warnOpenForPeriods = false;
  columnsToDisplay: string[];
  tableConfig: TableConfig[];

  constructor(private formService: FormService, private controlContainer: ControlContainer, private formBuilder: FormBuilder) {
    this.budgetForm = this.controlContainer.control as FormGroup;
  }

  onUnitCostChange(change: MatSelectChange, control: FormGroup, rowIndex: number): void {
    const selectedUnitCost = change.value as ProgrammeUnitCost;
    control.patchValue(
      {
        description: [],
        type: selectedUnitCost ? this.staffCostType.UNIT_COST : null,
        unitType: selectedUnitCost?.type || [],
        comment: [],
        numberOfUnits: 1,
        pricePerUnit: selectedUnitCost?.costPerUnit || 0,
        openForPeriods: 0,
      }
    );

    this.periods(rowIndex).controls.forEach(periodControl => {
      periodControl.get(this.constants.FORM_CONTROL_NAMES.amount)?.setValue(0);
    });
  }

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource<AbstractControl>(this.items.controls);
    this.numberOfItems$ = this.items.valueChanges.pipe(startWith(null), map(() => this.items.length));
    this.items.valueChanges.pipe(untilDestroyed(this)).subscribe(() => {
      this.dataSource.data = this.items.controls;
      this.items.controls.forEach(control => {
        this.setRowSum(control as FormGroup);
        this.setOpenForPeriods(control as FormGroup);
      });
      this.setTotal();
      this.setOpenForPeriodsWarning();
    });

    this.formService.reset$.pipe(
      map(() => this.resetStaffFormGroup(this.staffCostTable)),
      untilDestroyed(this)
    ).subscribe();

    const periodColumns = this.projectPeriods?.length
      ? [...this.projectPeriods?.map(period => 'period' + period.number), 'openForPeriods'] : [];
    this.columnsToDisplay = [
      'description', 'type', 'comment', 'unitType', 'numberOfUnits',
      'pricePerUnit', 'total', ...periodColumns, 'action',
    ];

    const periodWidths = this.projectPeriods?.length
      ? [...this.projectPeriods?.map(() => ({minInRem: 8})), {minInRem: 8}] : [];
    this.tableConfig = [
      {minInRem: 12}, {minInRem: 10}, {minInRem: 12}, {minInRem: 6},
      {minInRem: 5}, {minInRem: 8}, {minInRem: 8}, ...periodWidths, {minInRem: 3, maxInRem: 3}
    ];

    if (this.availableUnitCosts.length > 0) {
      this.columnsToDisplay.unshift('unitCost');
      this.tableConfig.unshift({minInRem: 10});
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.staffCostTable || changes.editable) {
      this.resetStaffFormGroup(this.staffCostTable);
    }
  }

  ngOnDestroy(): void {
    this.items.clear();
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
    this.formService.setDirty(true);
  }

  addNewItem(): void {
    this.items.push(this.formBuilder.group({
      id: null,
      description: [[]],
      type: [null],
      unitType: [[]],
      unitCost: [null],
      comment: [[]],
      numberOfUnits: [1, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
      pricePerUnit: [0, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
      rowSum: [0, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
      new: [true],
      budgetPeriods: this.formBuilder.array([]),
      openForPeriods: [0],
    }));
    this.formService.setDirty(true);
    this.addPeriods(this.items.length - 1);
  }

  private resetStaffFormGroup(staffTable: StaffCostsBudgetTable): void {
    this.total.setValue(staffTable.total, {emitEvent: false});
    this.items.clear();
    staffTable.entries.forEach(item => {
      this.items.push(this.formBuilder.group({
        id: [item.id],
        description: [item.description],
        type: [item.type],
        unitType: [item.unitType],
        unitCost: [this.availableUnitCosts.find(it => it.id === item.unitCostId) || null],
        comment: [item.comment],
        numberOfUnits: [item.numberOfUnits, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
        pricePerUnit: [item.pricePerUnit, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
        rowSum: [item.rowSum, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
        budgetPeriods: this.formBuilder.array([]),
        openForPeriods: [0],
      }));
      this.addPeriods(this.items.length - 1, item.budgetPeriods);
    });
    this.formService.resetEditable();
  }

  private setTotal(): void {
    let total = 0;
    this.items.controls.forEach(control => {
      total = NumberService.sum([control.get(this.constants.FORM_CONTROL_NAMES.rowSum)?.value || 0, total]);
    });
    this.total.setValue(NumberService.truncateNumber(total));
  }

  private setRowSum(control: FormGroup): void {
    const numberOfUnits = control.get(this.constants.FORM_CONTROL_NAMES.numberOfUnits)?.value || 0;
    const pricePerUnit = control.get(this.constants.FORM_CONTROL_NAMES.pricePerUnit)?.value || 0;
    control.get(this.constants.FORM_CONTROL_NAMES.rowSum)?.setValue(NumberService.truncateNumber(NumberService.product([numberOfUnits, pricePerUnit])), {emitEvent: false});
  }

  getUnitCost(formGroup: FormGroup): FormControl {
    return formGroup.get(this.constants.FORM_CONTROL_NAMES.unitCost) as FormControl;
  }

  get staff(): FormGroup {
    return this.budgetForm.get(this.constants.FORM_CONTROL_NAMES.staff) as FormGroup;
  }

  get items(): FormArray {
    return this.staff.get(this.constants.FORM_CONTROL_NAMES.items) as FormArray;
  }

  get total(): FormControl {
    return this.staff.get(this.constants.FORM_CONTROL_NAMES.total) as FormControl;
  }

  openForPeriods(rowIndex: number): FormControl {
    return this.items.at(rowIndex).get(this.constants.FORM_CONTROL_NAMES.openForPeriods) as FormControl;
  }

  periods(rowIndex: number): FormArray {
    return this.items.at(rowIndex).get(this.constants.FORM_CONTROL_NAMES.budgetPeriods) as FormArray;
  }

  periodTotal(periodIndex: number): number {
    let total = 0;
    this.items.controls.forEach(control => {
      const periods = control.get(this.constants.FORM_CONTROL_NAMES.budgetPeriods) as FormArray;
      const periodAmount = periods?.at(periodIndex - 1)?.get(this.constants.FORM_CONTROL_NAMES.amount)?.value;
      total = NumberService.sum([periodAmount || 0, total]);
    });
    return total;
  }

  private addPeriods(rowIndex: number, budgetPeriods?: BudgetPeriodDTO[]): void {
    if (!this.projectPeriods?.length) {
      return;
    }
    this.projectPeriods.forEach(projectPeriod => {
      const budgetPeriod = budgetPeriods?.find(period => period.number === projectPeriod.number);
      this.periods(rowIndex).push(this.formBuilder.group({
        amount: this.formBuilder.control(
          budgetPeriod?.amount || 0,
          [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]
        ),
        number: this.formBuilder.control(projectPeriod.number)
      }));
    });
  }

  private setOpenForPeriodsWarning(): void {
    if (!this.projectPeriods?.length) {
      return;
    }
    this.warnOpenForPeriods = this.items.controls.some(
      control => control.get(this.constants.FORM_CONTROL_NAMES.openForPeriods)?.value !== 0
    );
  }

  private setOpenForPeriods(control: FormGroup): void {
    let periodsSum = 0;
    (control.get(this.constants.FORM_CONTROL_NAMES.budgetPeriods) as FormArray).controls.forEach(period => {
      periodsSum = NumberService.sum([period.get(this.constants.FORM_CONTROL_NAMES.amount)?.value || 0, periodsSum]);
    });
    const rowSum = control.get(this.constants.FORM_CONTROL_NAMES.rowSum)?.value || 0;
    control.get(this.constants.FORM_CONTROL_NAMES.openForPeriods)?.setValue(
      NumberService.minus(rowSum, periodsSum), {emitEvent: false}
    );
  }
}
