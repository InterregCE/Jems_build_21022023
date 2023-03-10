import {ChangeDetectionStrategy, Component} from '@angular/core';
import {IndicatorOverviewLine} from './models/IndicatorOverviewLine';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {RowSpanPlan} from '@project/project-overview-tables-page/project-application-form-a4/models/RowSpanPlan';
import {IndicatorOverviewLineDTO} from '@cat/api';
import {Alert} from '@common/components/forms/alert';
import {ProjectOverviewTablesPageStore} from '@project/project-overview-tables-page/project-overview-tables-page-store.service';

@Component({
  selector: 'jems-project-application-form-a4',
  templateUrl: './project-application-form-a4.component.html',
  styleUrls: ['./project-application-form-a4.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFormA4Component {
  Alert = Alert;
  displayedColumns: string[] = [
    'outputIndicatorName',
    'outputIndicatorTargetValueSumUp',
    'outputIndicatorMeasurementUnit',
    'projectOutputNumber',
    'projectOutputTitle',
    'projectOutputTargetValue',
    'resultIndicatorName',
    'resultIndicatorBaseline',
    'resultIndicatorTargetValueSumUp',
    'resultIndicatorMeasurementUnit',
  ];
  spans: RowSpanPlan = {
    resultIndicator: [],
    outputIndicator: [],
  };

  indicatorOverviewLines$: Observable<IndicatorOverviewLineDTO[]>;

  private MAX_INDICATOR_ID_FROM_DB = 1_000_000_000_000;
  private MAX_INDICATOR_FAKE_ID = 3_000_000_000_000;

  constructor(private pageStore: ProjectOverviewTablesPageStore) {
    this.indicatorOverviewLines$ = this.pageStore.indicatorOverviewLines$
      .pipe(
        map((data: IndicatorOverviewLineDTO[]) => this.transformIdsOfIndicatorsToRowIds(data)),
        map(data => data.sort(this.sortByResultThenOutputIndicators)),
        // build row-span plans
        tap(data => this.spans.resultIndicator = this.getRowSpanPlan(['resultIndicatorId'], data)),
        tap(data => this.spans.outputIndicator = this.getRowSpanPlan(['resultIndicatorId', 'outputIndicatorId'], data)),
      );
  }

  getRowSpan(path: string, index: number): number {
    switch (path) {
      case 'outputIndicator':
        return this.spans.outputIndicator[index];
      case 'resultIndicator':
        return this.spans.resultIndicator[index];
      default:
        return 1;
    }
  }

  /**
   * Calculate rowspan within the group for every row recursively
   * e.g. [2,0,1] means first row span 2 rows, second is hidden, third span 1 row normally
   *
   * @param attributes - ordered table columns, on those we are creating tree structure recursively
   * @param data - table rows
   */
  getRowSpanPlan(attributes: string[] | null, data: IndicatorOverviewLine[]): number[] {

    // if we do not go deeper, stop recursion and fill row-spans for this group
    if (!attributes?.length) {
      return data.map(() => 0).fill(data.length, 0, 1);
    }

    const attributesToFollow: string[] = [...attributes];
    const attribute = attributesToFollow.shift() || ''; // this || is because of tslint, it cannot happen due to previous condition

    const uniqueRowsWithinGroup = data.reduce(
      (unique: IndicatorOverviewLine[], item: IndicatorOverviewLine) =>
        unique.find(x => this.getValue(attribute, x) === this.getValue(attribute, item)) ? unique : [...unique, item],
      [],
    );

    return uniqueRowsWithinGroup
      .map((uniqItem) =>
        (this.getRowSpanPlan(
          attributesToFollow,
          data.filter((item) => this.getValue(attribute, uniqItem) === this.getValue(attribute, item))
        ) as any) // lint fix -> wtf is this code doing anyway?
      )
      .flat(attributes.length);
  }

  private getValue(attribute: string, from: IndicatorOverviewLine): number {
    switch (attribute) {
      case 'outputIndicatorId':
        return from.outputIndicatorId;
      case 'resultIndicatorId':
        return from.resultIndicatorId;
      default:
        return -1;
    }
  }

  private sortByResultThenOutputIndicators = (a: IndicatorOverviewLine, b: IndicatorOverviewLine) => {
    if (a.resultIndicatorId !== b.resultIndicatorId) {
      return a.resultIndicatorId - b.resultIndicatorId;
    }
    if (a.outputIndicatorId !== b.outputIndicatorId) {
      return a.outputIndicatorId - b.outputIndicatorId;
    }
    return 1;
  };

  /**
   * This function will assign IDs to those indicators or outputs, that do not exist, just
   * to be able to sort them properly and calculate row-spans correctly.
   *
   * Firstly we are using those that has result indicators assigned, then those without, and finally
   * we use result indicators without Outputs.
   *
   * They will get unique ID based on their index
   *
   * @param data to be transformed
   */
  private transformIdsOfIndicatorsToRowIds(data: IndicatorOverviewLineDTO[]): IndicatorOverviewLineDTO[] {
    return data.map((currElem, index) => {
      if (currElem.onlyResultWithoutOutputs) {
        return {
          ...currElem,
          outputIndicatorId: this.MAX_INDICATOR_FAKE_ID + index,
          resultIndicatorId: this.MAX_INDICATOR_FAKE_ID + index,
        };
      }

      // if not linked, assign custom from (bigger than) MAX_INDICATOR_ID_FROM_DB
      const outputIndicatorId = currElem.outputIndicatorId || (this.MAX_INDICATOR_ID_FROM_DB + index);
      return {
        ...currElem,
        outputIndicatorId,
        // if not linked, calculate custom
        resultIndicatorId: currElem.resultIndicatorId || this.MAX_INDICATOR_ID_FROM_DB + outputIndicatorId,
      };
    });
  }

}
