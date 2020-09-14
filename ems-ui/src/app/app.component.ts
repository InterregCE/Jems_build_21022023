import {Component} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Title} from '@angular/platform-browser';
import {Router} from '@angular/router';
import {SideNavService} from '@common/components/side-nav/side-nav.service';
import {BaseComponent} from '@common/components/base-component';

@Component({
  selector: 'app-root',
  styleUrls: ['./app.component.scss'],
  templateUrl: './app.component.html'
})
export class AppComponent extends BaseComponent {
  isLoginNeeded = true;

  headlines$ = this.sideNavService.getHeadlines();

  constructor(public translate: TranslateService,
              private titleService: Title,
              private router: Router,
              public sideNavService: SideNavService) {
    super();

    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.titleService.setTitle('Ems');
  }
}
