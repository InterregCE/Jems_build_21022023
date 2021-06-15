import {ProgrammeLumpSum} from './lump-sums/programmeLumpSum';
import {ProgrammeUnitCost} from './programmeUnitCost';
import {CallFlatRateSetting} from './call-flat-rate-setting';
import {ApplicationFormConfigurationDTO} from '@cat/api';

export class ProjectCallSettings {
  callId: number;
  callName: string;
  startDate: Date;
  endDate: Date;
  endDateStep1: Date;
  lengthOfPeriod: number;
  flatRates: CallFlatRateSetting;
  lumpSums: Array<ProgrammeLumpSum>;
  unitCosts: Array<ProgrammeUnitCost>;
  multipleFundsAllowed: boolean;
  applicationFormConfiguration: ApplicationFormConfigurationDTO;

  constructor(callId: number, callName: string, startDate: Date, endDate: Date, endDateStep1: Date, lengthOfPeriod: number, flatRates: CallFlatRateSetting, lumpSums: Array<ProgrammeLumpSum>, unitCosts: Array<ProgrammeUnitCost>, multipleFundsAllowed: boolean, applicationFormConfiguration: ApplicationFormConfigurationDTO) {
    this.callId = callId;
    this.callName = callName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.endDateStep1 = endDateStep1;
    this.lengthOfPeriod = lengthOfPeriod;
    this.flatRates = flatRates;
    this.lumpSums = lumpSums;
    this.unitCosts = unitCosts;
    this.multipleFundsAllowed = multipleFundsAllowed;
    this.applicationFormConfiguration = applicationFormConfiguration;
  }
}
