import {Routes} from '@angular/router';
import {AppWrapComponent} from './component/app-wrap/app-wrap.component';
import {NoAuthWrapComponent} from './component/no-auth-wrap/no-auth-wrap.component';
import {AppNotFoundComponent} from './component/app-not-found/app-not-found.component';
import {AuthenticationGuard} from './security/authentication-guard.service';
import {Permission} from './security/permissions/permission';
import {PermissionGuard} from './security/permission.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'app',
  },
  {
    path: 'login',
    pathMatch: 'full',
    redirectTo: 'no-auth/login',
  },
  {
    path: 'app',
    component: AppWrapComponent,
    canActivate: [AuthenticationGuard],
    data: {
      breadcrumb: 'common.breadcrumb.home'
    },
    children: [
      {path: '', pathMatch: 'full', redirectTo: 'project'},
      {
        path: 'call',
        loadChildren: () => import('./call/call.module').then(m => m.CallModule),
        data: {skipBreadcrumb: true},
      },
      {
        path: 'project',
        loadChildren: () => import('./project/project.module').then(m => m.ProjectModule),
        data: {skipBreadcrumb: true},
      },
      {
        path: 'programme',
        loadChildren: () => import('./programme/programme.module').then(m => m.ProgrammeModule),
        data: {
          skipBreadcrumb: true,
          permissionsOnly: [Permission.ADMINISTRATOR, Permission.PROGRAMME_USER],
        },
        canActivate: [PermissionGuard]
      },
      {
        path: 'user',
        loadChildren: () => import('./user/user.module').then(m => m.UserModule),
        data: {
          skipBreadcrumb: true,
          permissionsOnly: [Permission.ADMINISTRATOR],
        },
        canActivate: [PermissionGuard],
      },
      {
        path: 'profile',
        loadChildren: () => import('./user-profile/user-profile.module').then(m => m.UserProfileModule),
        data: {skipBreadcrumb: true},
      },
    ]
  },
  {
    path: 'no-auth',
    component: NoAuthWrapComponent,
    children: [
      {
        path: '',
        loadChildren: () => import('./authentication/authentication.module').then(m => m.AuthenticationModule),
      }
    ]
  },
  {
    path: '**',
    component: NoAuthWrapComponent,
    children: [
      {
        path: '',
        component: AppNotFoundComponent,
      }
    ]
  },
];