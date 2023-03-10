import {Injectable} from '@angular/core';
import {CallDetailDTO, PluginInfoDTO, PreSubmissionPluginsDTO} from '@cat/api';
import {Observable} from 'rxjs';
import {CallStore} from '../services/call-store.service';
import {map} from 'rxjs/operators';

import {PluginStore} from '@common/services/plugin-store.service';
import {PluginKeys} from './plugin-keys';
import TypeEnum = PluginInfoDTO.TypeEnum;

@Injectable()
export class PreSubmissionCheckSettingsPageStore {

  preSubmissionCheckPlugins: Observable<PluginInfoDTO[]>;
  reportPartnerCheckPlugins: Observable<PluginInfoDTO[]>;
  callIsEditable$: Observable<boolean>;
  callHasTwoSteps$: Observable<boolean>;
  pluginKeys$: Observable<PluginKeys>;

  constructor(private pluginStore: PluginStore,
              private callStore: CallStore) {
    this.preSubmissionCheckPlugins = this.pluginStore.getPluginListByType(TypeEnum.PRESUBMISSIONCHECK);
    this.reportPartnerCheckPlugins = this.pluginStore.getPluginListByType(TypeEnum.REPORTPARTNERCHECK);
    this.callIsEditable$ = this.callStore.callIsEditable$;
    this.callHasTwoSteps$ = this.callStore.call$.pipe(map(call => !!call.endDateTimeStep1));
    this.pluginKeys$ = this.callStore.call$.pipe(map((call) => ({
      pluginKey: call.preSubmissionCheckPluginKey,
      firstStepPluginKey: call.firstStepPreSubmissionCheckPluginKey,
      reportPartnerCheckPluginKey: call.reportPartnerCheckPluginKey,
      callHasTwoSteps: !!call.endDateTimeStep1
    })));
  }

  save(pluginKeys: PreSubmissionPluginsDTO): Observable<CallDetailDTO> {
    return this.callStore.savePreSubmissionCheckSettings(pluginKeys);
  }
}
