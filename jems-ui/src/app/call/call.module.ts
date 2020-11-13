import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {routes} from './call-routing.module';
import {CallPageComponent} from './containers/call-page/call-page.component';
import {CallListComponent} from './components/call-list/call-list.component';
import {SharedModule} from '../common/shared-module';
import {CallDetailComponent} from './components/call-detail/call-detail.component';
import {CallConfigurationComponent} from './containers/call-configuration/call-configuration.component';
import {CallStore} from './services/call-store.service';
import {CallActionCellComponent} from './components/call-list/call-action-cell/call-action-cell.component';
import {CallPriorityTreeComponent} from './components/call-priority-tree/call-priority-tree.component';
import {CallNameResolver} from './services/call-name.resolver';
import {CallStrategiesComponent} from './components/call-detail/call-strategies/call-strategies.component';
import {CallFundsComponent} from './components/call-detail/call-funds/call-funds.component';
import { CallFlatRatesComponent } from './components/call-detail/call-flat-rates/call-flat-rates.component';
import { CallFlatRatesToggleColumnComponent } from './components/call-detail/call-flat-rates/call-flat-rates-toggle-column/call-flat-rates-toggle-column.component';
import { CallFlatRatesPageComponent } from './containers/call-flat-rates-page/call-flat-rates-page.component';
import {CallPageSidenavService} from './services/call-page-sidenav.service';

@NgModule({
  declarations: [
    CallPageComponent,
    CallListComponent,
    CallDetailComponent,
    CallConfigurationComponent,
    CallActionCellComponent,
    CallPriorityTreeComponent,
    CallStrategiesComponent,
    CallFundsComponent,
    CallFlatRatesComponent,
    CallFlatRatesToggleColumnComponent,
    CallFlatRatesPageComponent,
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ],
  providers: [
    CallStore,
    CallNameResolver,
    CallPageSidenavService
  ],
})
export class CallModule {
}
