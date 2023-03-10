import {Injectable} from '@angular/core';
import {merge, Observable, of, Subject} from 'rxjs';
import {OutputCurrentUser, UserRoleCreateDTO, UserRoleDTO, UserRoleService} from '@cat/api';
import {filter, map, shareReplay, switchMap, take, tap} from 'rxjs/operators';
import {Log} from '@common/utils/log';
import {SecurityService} from '../../../security/security.service';
import {RoutingService} from '@common/services/routing.service';
import {RoleStore} from '../../services/role-store.service';

@Injectable()
export class UserRoleDetailPageStore {
  public static USER_ROLE_DETAIL_PATH = '/app/system/role/detail';

  userRole$: Observable<UserRoleDTO>;
  currentUser$: Observable<OutputCurrentUser | null>;
  userRoleName$: Observable<string>;

  private savedUserRole$ = new Subject<UserRoleDTO>();

  constructor(private roleService: UserRoleService,
              private router: RoutingService,
              private securityService: SecurityService,
              private roleStore: RoleStore) {
    this.userRole$ = this.userRole();
    this.userRoleName$ = this.userRoleName();
    this.currentUser$ = this.securityService.currentUser;
  }

  createUserRole(user: UserRoleCreateDTO): Observable<UserRoleDTO> {
    return this.roleService.createUserRole(user)
      .pipe(
        take(1),
        tap(saved => Log.info('Created user role:', this, saved)),
        tap(() => this.roleStore.rolesChanged$.next())
      );
  }

  saveUserRole(user: UserRoleDTO): Observable<UserRoleDTO> {
    return this.roleService.updateUserRole(user)
      .pipe(
        tap(saved => this.savedUserRole$.next(saved)),
        tap(saved => Log.info('Updated user role:', this, saved)),
        tap(() => this.roleStore.rolesChanged$.next())
      );
  }

  private userRole(): Observable<UserRoleDTO> {
    const initialUserRole$ = this.router.routeParameterChanges(UserRoleDetailPageStore.USER_ROLE_DETAIL_PATH, 'roleId')
      .pipe(
        switchMap(roleId => roleId ? this.roleService.getById(Number(roleId)) : of({} as UserRoleDTO)),
        tap(user => Log.info('Fetched the user role:', this, user))
      );

    return merge(initialUserRole$, this.savedUserRole$)
      .pipe(
        shareReplay(1),
      );
  }

  private userRoleName(): Observable<string> {
    return this.userRole$.pipe(
      filter(role => !!role.name),
      map(role => role.name),
    );
  }

}
