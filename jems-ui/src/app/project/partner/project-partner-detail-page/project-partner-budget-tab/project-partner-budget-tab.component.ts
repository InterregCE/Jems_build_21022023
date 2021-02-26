import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ProjectPartnerStore} from '../../../project-application/containers/project-application-form-page/services/project-partner-store.service';
import {ProjectStore} from '../../../project-application/containers/project-application-detail/services/project-store.service';

@Component({
  selector: 'app-project-partner-budget-tab',
  templateUrl: './project-partner-budget-tab.component.html',
  styleUrls: ['./project-partner-budget-tab.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectPartnerBudgetTabComponent {

  constructor(public projectStore: ProjectStore,
              public partnerStore: ProjectPartnerStore) {
  }

}
