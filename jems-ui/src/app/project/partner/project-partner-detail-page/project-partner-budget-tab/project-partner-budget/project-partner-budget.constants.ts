import {NumberService} from '../../../../../common/services/number.service';
import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';
import {AllowedBudgetCategory} from '@project/model/allowed-budget-category';

export class ProjectPartnerBudgetConstants {

  public static MAX_NUMBER_OF_ITEMS = 300;
  public static MAX_STAFF_COMMENTS_TEXT_LENGTH = 250;
  public static MAX_UNIT_TYPE_TEXT_LENGTH = 100;
  public static MAX_AWARD_PROCEDURES_TEXT_LENGTH = 250;
  public static MAX_VALUE = 999_999_999.99;
  public static MIN_VALUE = 0;
  public static FORM_CONTROL_NAMES = {
    staff: 'staff',
    travel: 'travel',
    external: 'external',
    equipment: 'equipment',
    infrastructure: 'infrastructure',
    unitCost: 'unitCost',
    items: 'items',
    numberOfUnits: 'numberOfUnits',
    pricePerUnit: 'pricePerUnit',
    description: 'description',
    type: 'type',
    unitType: 'unitType',
    awardProcedures: 'awardProcedures',
    investmentId: 'investmentId',
    comments: 'comments',
    rowSum: 'rowSum',
    total: 'total',
    budgetPeriods: 'budgetPeriods',
    amount: 'amount',
    openForPeriods: 'openForPeriods'
  };

  public static SPF_FORM_CONTROL_NAMES = {
    spf: 'spf',
    total: 'total',
    comments: 'comments',
    numberOfUnits: 'numberOfUnits',
    pricePerUnit: 'pricePerUnit',
    description: 'description',
    budgetPeriods: 'budgetPeriods',
    amount: 'amount',
    items: 'items',
    openForPeriods: 'openForPeriods',
    unitType: 'unitType',
    rowSum: 'rowSum',
  };

  public static FORM_ERRORS = {
    total: {
      max: 'project.partner.budget.table.total.max.invalid'
    },
    unitCost: {
      required: 'project.partner.budget.table.unitCost.required',
    },
  };

  public static FORM_ERRORS_ARGS = {
    total: {
      max: {maxValue: NumberService.toLocale(ProjectPartnerBudgetConstants.MAX_VALUE)}
    },
  };

  public static requiredUnitCost(allowedBudgetCategory: AllowedBudgetCategory): ValidatorFn | null {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!allowedBudgetCategory.unitCostsOnly() || control.value) {
        return null;
      }
      return {required: true} as any;
    };
  }

}
