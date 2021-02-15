import {ProjectResultDTO, ProjectWorkPackageDTO} from '@cat/api';
import {DataSet} from 'vis-data/peer';
import * as moment from 'moment';
import {Moment} from 'moment';
import {TimelineOptions, TimelineTimeAxisScaleType} from 'vis-timeline';

export const colors = [
  'bg-purple',
  'bg-cyan'
];

export const EMPTY_STRING = '&nbsp';
export const START_DATE = '2000-01-01';

function getColor(index: number): string {
  return colors[index % colors.length];
}

export function getStartDateFromPeriod(period: number): Moment {
  return moment(START_DATE).utc().add(period, 'M').startOf('month');
}

export function getEndDateFromPeriod(period: number): Moment {
  return moment(START_DATE).utc().add(period, 'M').endOf('month');
}

export function getNestedStartDateFromPeriod(period: number): Moment {
  return getStartDateFromPeriod(period).add(1, 'd');
}

export function getNestedEndDateFromPeriod(period: number): Moment {
  return getEndDateFromPeriod(period).subtract(1, 'd');
}

export function periodLabelFunction(date: Date, scale: string, step: number): string {
  const periodNumber = Math.round(moment(date).utc().diff(
    moment('2000-01-01').utc(), 'months', true) + 1
  );
  return `Period ${periodNumber}`;
}

function getWorkPackageId(indexWp: number): number {
  return (indexWp + 1) * 100_000;
}

function getResultId(indexResult: number): number {
  return (indexResult + 1) * 1_000;
}

function getActivityId(indexWp: number, indexActivity: number): number {
  return getWorkPackageId(indexWp) + 10_000 + (indexActivity + 1) * 100;
}

function getActivityBoxId(indexWp: number, indexActivity: number): number {
  return getActivityId(indexWp, indexActivity) + 50;
}

function getDeliverableBoxId(indexWp: number, indexActivity: number, indexDeliverable: number): number {
  return getActivityBoxId(indexWp, indexActivity) + (indexDeliverable + 1);
}

function getOutputId(indexWp: number, indexOutput: number): number {
  return getWorkPackageId(indexWp) + 20_000 + (indexOutput + 1) * 100;
}

function getOutputBoxId(indexWp: number, indexOutput: number): number {
  return getOutputId(indexWp, indexOutput) + 50;
}

/**
 * Generate items = boxes visible inside timeline
 *
 * currently we are generating them for:
 *   - the whole WorkPackage
 *     - activity
 *       - deliverable
 *     - output
 *   - result indicator //TODO
 */
export function getItems(timePlan: ProjectTimePlan): DataSet<any> {
  let items = new Array(0);
  timePlan.workPackages.forEach((workPackage, indexWp) => {
    let minPeriod = 999;
    let maxPeriod = 0;

    workPackage.activities.forEach((activity, indexActivity) => {
      items = items.concat({
        id: getActivityBoxId(indexWp, indexActivity),
        group: getActivityId(indexWp, indexActivity),
        start: getStartDateFromPeriod(activity.startPeriod),
        end: getEndDateFromPeriod(activity.endPeriod),
        type: 'background',
        className: getColor(indexWp),
      });

      activity.deliverables.forEach((deliverable, indexDeliverable) => {
        items = items.concat({
          id: getDeliverableBoxId(indexWp, indexActivity, indexDeliverable),
          group: getActivityId(indexWp, indexActivity),
          start: getNestedStartDateFromPeriod(deliverable.period),
          end: getNestedEndDateFromPeriod(deliverable.period),
          type: 'range',
          content: `D${indexWp + 1}.${indexActivity + 1}.${indexDeliverable + 1}`,
          className: getColor(indexWp),
        });
      });

      if (minPeriod > activity.startPeriod) {
        minPeriod = activity.startPeriod;
      }
      if (maxPeriod < activity.endPeriod) {
        maxPeriod = activity.endPeriod;
      }
    });

    workPackage.outputs.forEach((output, indexOutput) => {
      items = items.concat({
        id: getOutputBoxId(indexWp, indexOutput),
        group: getOutputId(indexWp, indexOutput),
        start: getNestedStartDateFromPeriod(output.periodNumber),
        end: getNestedEndDateFromPeriod(output.periodNumber),
        type: 'range',
        title: `<span>Target value: ${output.targetValue}<br>Period: ${output.periodNumber}</span>`,
        content: `O.${indexOutput + 1}`,
        className: getColor(indexWp),
      });
    });

    if (minPeriod !== 999 && maxPeriod !== 0) {
      items = items.concat({
        id: indexWp + 1,
        group: getWorkPackageId(indexWp),
        start: getStartDateFromPeriod(minPeriod),
        end: getEndDateFromPeriod(maxPeriod),
        type: 'background',
        className: getColor(indexWp),
      });
    }
  });

  return new DataSet(items);
}

/**
 * Generate groups = group represents 1 swim lane inside timeline (the first left collapsable column)
 *
 * currently we are generating them for:
 *   - the whole WorkPackage
 *     - activity
 *     - output
 *   - result indicator //TODO
 */
export function getGroups(timePlan: ProjectTimePlan): DataSet<any> {
  let wpGrouped = new Array(0);
  const groups = timePlan.workPackages.map((workPackage, indexWp) => {
    const activities = workPackage.activities.map((activity, indexActivity) => {
      return {
        id: getActivityId(indexWp, indexActivity),
        content: EMPTY_STRING,
        treeLevel: 2,
      };
    });
    wpGrouped = wpGrouped.concat(activities);

    const outputs = workPackage.outputs.map((output, indexOutput) => {
      return {
        id: getOutputId(indexWp, indexOutput),
        content: EMPTY_STRING,
        treeLevel: 2,
      };
    });
    wpGrouped = wpGrouped.concat(outputs);

    return {
      id: getWorkPackageId(indexWp),
      content: EMPTY_STRING,
      treeLevel: 1,
      nestedGroups: activities.map(activity => activity.id).concat(outputs.map(output => output.id)),
    };
  });

  return new DataSet(groups.concat(wpGrouped));
}

export class ProjectTimePlan {
  workPackages: ProjectWorkPackageDTO[];
  results: ProjectResultDTO[];
}

export class Content {
  id: number;
  content: string;
}

export function getTranslations(timePlan: ProjectTimePlan): { [language: string]: Content[]; } {
  const languages: { [language: string]: Content[]; } = {};
  timePlan.workPackages.forEach((workPackage, indexWp) => {
    workPackage.name.forEach(translation => {
      if (!languages[translation.language]) {
        languages[translation.language] = new Array<Content>(0);
      }
      languages[translation.language].push({
        id: getWorkPackageId(indexWp),
        content: translation.translation
      } as Content);
    });

    workPackage.activities.forEach((activity, indexActivity) => {
      activity.title.forEach(translation => {
        if (!languages[translation.language]) {
          languages[translation.language] = new Array<Content>(0);
        }
        languages[translation.language].push({
          id: getActivityId(indexWp, indexActivity),
          content: translation.translation
        } as Content);
      });
    });

    workPackage.outputs.forEach((output, indexOutput) => {
      output.title.forEach(translation => {
        if (!languages[translation.language]) {
          languages[translation.language] = new Array<Content>(0);
        }
        languages[translation.language].push({
          id: getOutputId(indexWp, indexOutput),
          content: translation.translation
        } as Content);
      });
    });
  });
  return languages;
}

export function getOptions(custom?: Partial<TimelineOptions>): TimelineOptions {
  return Object.assign(
    {
      showCurrentTime: false,
      showMajorLabels: false,
      orientation: 'top',
      timeAxis: {scale: 'month' as TimelineTimeAxisScaleType, step: 1},
      format: {minorLabels: periodLabelFunction},
      margin: {
        axis: 10,
        item: {vertical: 10, horizontal: 0}
      },
      min: START_DATE,
    },
    custom
  );
}