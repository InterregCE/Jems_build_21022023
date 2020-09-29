import {ChangeDetectionStrategy, Component} from '@angular/core';
import {SideNavService} from '@common/components/side-nav/side-nav.service';
import {SecurityService} from 'src/app/security/security.service';

@Component({
  selector: 'app-wrap',
  templateUrl: './app-wrap.component.html',
  styleUrls: ['./app-wrap.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppWrapComponent {

  headlines$ = this.sideNavService.getHeadlines();

  constructor(public sideNavService: SideNavService,
              public securityService: SecurityService) {
  }

}