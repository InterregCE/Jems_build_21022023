import {NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {TopBarComponent} from './components/top-bar/top-bar.component';
import {MenuComponent} from './components/menu/menu.component';
import {SharedModule} from './shared-module';
import {SecurityService} from '../security/security.service';
import {AuthenticationInterceptor} from '../security/authentication.interceptor';
import {HttpErrorInterceptor} from './interceptors/http-error.interceptor';
import {MatTabsModule} from '@angular/material/tabs';

const declarations = [
  TopBarComponent,
  MenuComponent,
];

@NgModule({
  declarations: [
    declarations
  ],
  imports: [
    SharedModule,
    MatTabsModule
  ],
  providers: [
    SecurityService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthenticationInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpErrorInterceptor,
      multi: true
    }
  ],
  exports: [
    declarations
  ]
})
export class CoreModule {
}