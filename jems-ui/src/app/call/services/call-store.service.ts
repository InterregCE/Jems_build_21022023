import {Injectable} from '@angular/core';
import {
  CallDetailDTO,
  CallService,
  FlatRateSetupDTO,
  ProgrammeCostOptionService,
  ProgrammeLumpSumListDTO,
  ProgrammeUnitCostListDTO,
  UserRoleCreateDTO
} from '@cat/api';
import {merge, Observable, of, Subject} from 'rxjs';
import {map, shareReplay, switchMap, tap} from 'rxjs/operators';
import {Log} from '@common/utils/log';
import {PermissionService} from '../../security/permissions/permission.service';
import {RoutingService} from '@common/services/routing.service';
import PermissionsEnum = UserRoleCreateDTO.PermissionsEnum;

@Injectable()
export class CallStore {
  public static CALL_DETAIL_PATH = '/app/call/detail';
  private callId: number;
  call$: Observable<CallDetailDTO>;
  unitCosts$: Observable<ProgrammeUnitCostListDTO[]>;
  lumpSums$: Observable<ProgrammeLumpSumListDTO[]>;
  userCanApply$: Observable<boolean>;
  callIsReadable$: Observable<boolean>;
  callIsEditable$: Observable<boolean>;
  callIsPublished$: Observable<boolean>;

  savedCall$ = new Subject<CallDetailDTO>();

  constructor(private callService: CallService,
              private programmeCostOptionService: ProgrammeCostOptionService,
              private permissionService: PermissionService,
              private router: RoutingService) {
    this.call$ = this.call();
    this.unitCosts$ = this.unitCosts();
    this.lumpSums$ = this.lumpSums();
    this.userCanApply$ = this.permissionService.hasPermission(PermissionsEnum.ProjectCreate);
    this.callIsEditable$ = this.permissionService.hasPermission(PermissionsEnum.CallUpdate);
    this.callIsReadable$ = this.permissionService.hasPermission(PermissionsEnum.CallRetrieve);
    this.callIsPublished$ = this.callIsPublished();
  }

  saveFlatRates(flatRates: FlatRateSetupDTO): Observable<CallDetailDTO> {
    return this.callService.updateCallFlatRateSetup(this.callId, flatRates)
      .pipe(
        tap(saved => this.savedCall$.next(saved)),
        tap(saved => Log.info('Updated call flat rates:', this, saved))
      );
  }

  saveLumpSums(lumpSumIds: number[]): Observable<CallDetailDTO> {
    return this.callService.updateCallLumpSums(this.callId, lumpSumIds)
      .pipe(
        tap(saved => this.savedCall$.next(saved)),
        tap(saved => Log.info('Updated call lump sums:', this, saved))
      );
  }

  saveUnitCosts(unitCostIds: number[]): Observable<CallDetailDTO> {
    return this.callService.updateCallUnitCosts(this.callId, unitCostIds)
      .pipe(
        tap(saved => this.savedCall$.next(saved)),
        tap(saved => Log.info('Updated call unit costs:', this, saved))
      );
  }

  private call(): Observable<CallDetailDTO> {
    const initialCall$ = this.router.routeParameterChanges(CallStore.CALL_DETAIL_PATH, 'callId')
      .pipe(
        switchMap(id => id ? this.callService.getCallById(Number(id)) : of({} as CallDetailDTO)),
        tap(call => this.callId = (call as any)?.id),
        tap(call => Log.info('Fetched the call:', this, call)),
      );
    return merge(initialCall$, this.savedCall$)
      .pipe(
        shareReplay(1)
      );
  }

  private unitCosts(): Observable<ProgrammeUnitCostListDTO[]> {
    return this.programmeCostOptionService.getProgrammeUnitCosts()
      .pipe(
        tap(list => Log.info('Fetched the Unit Costs:', this, list)),
      );
  }

  private lumpSums(): Observable<ProgrammeLumpSumListDTO[]> {
    return this.programmeCostOptionService.getProgrammeLumpSums()
      .pipe(
        tap(list => Log.info('Fetched the Lump Sums:', this, list))
      );
  }

  private callIsPublished(): Observable<boolean> {
    return this.call$
      .pipe(
        map(call => call?.status === CallDetailDTO.StatusEnum.PUBLISHED)
      );
  }
}
