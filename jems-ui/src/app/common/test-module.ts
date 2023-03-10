import {NgModule} from '@angular/core';
import {SharedModule} from './shared-module';
import {DatePipe} from '@angular/common';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {
  NgxPermissionsConfigurationStore,
  NgxPermissionsModule,
  NgxPermissionsService,
  NgxPermissionsStore,
  NgxRolesStore
} from 'ngx-permissions';
import {LoginPageService} from '../authentication/login/services/login-page-service';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {SideNavService} from '@common/components/side-nav/side-nav.service';
import {ApiModule} from '@cat/api';
import {LanguageStore} from './services/language-store.service';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {HttpLoaderFactory} from '../app-i18n.module';
import {HttpClient} from '@angular/common/http';

@NgModule({
  declarations: [],
  imports: [
    ApiModule,
    SharedModule,
    HttpClientTestingModule,
    RouterTestingModule,
    HttpClientTestingModule,
    NgxPermissionsModule,
    NoopAnimationsModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
  ],
  providers: [
    DatePipe,
    NgxPermissionsStore,
    NgxPermissionsConfigurationStore,
    NgxRolesStore,
    NgxPermissionsService,
    LoginPageService,
    LoginPageService,
    SideNavService,
    LanguageStore,
  ],
  exports: [
    TranslateModule,
  ]
})
export class TestModule {
}
