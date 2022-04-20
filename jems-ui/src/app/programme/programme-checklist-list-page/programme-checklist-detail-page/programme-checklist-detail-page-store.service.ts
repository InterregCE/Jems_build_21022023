import {Injectable} from '@angular/core';
import {ProgrammeChecklistDetailDTO, ProgrammeChecklistDTO, ProgrammeChecklistService} from '@cat/api';
import {RoutingService} from '@common/services/routing.service';
import {Log} from '@common/utils/log';
import {combineLatest, merge, Observable, of, Subject} from 'rxjs';
import {map, share, switchMap, take, tap} from 'rxjs/operators';
import {ProgrammeEditableStateStore} from '../../programme-page/services/programme-editable-state-store.service';

@Injectable()
export class ProgrammeChecklistDetailPageStore {
  static readonly CHECKLIST_DETAIL_PATH = '/app/programme/checklists/';

  checklist$: Observable<ProgrammeChecklistDetailDTO>;
  isEditable$: Observable<boolean>;

  private savedChecklist$ = new Subject<ProgrammeChecklistDetailDTO>();

  constructor(private programmeChecklistService: ProgrammeChecklistService,
              private routingService: RoutingService,
              private programmeEditableStateStore: ProgrammeEditableStateStore) {
    this.checklist$ = this.checklist();
    this.isEditable$ = this.isEditable();
  }

  saveChecklist(checklist: ProgrammeChecklistDetailDTO): Observable<ProgrammeChecklistDTO> {
    return this.checkListId()
      .pipe(
        take(1),
        switchMap(id => id
          ? this.programmeChecklistService.updateProgrammeChecklist(checklist)
          : this.programmeChecklistService.createProgrammeChecklist(checklist)
            .pipe(
              tap(createdChecklist => this.routingService.navigate(
                [ProgrammeChecklistDetailPageStore.CHECKLIST_DETAIL_PATH + createdChecklist.id]
                )
              )
            )
        ),
        tap((saved: ProgrammeChecklistDetailDTO) => this.savedChecklist$.next(saved)),
        tap(saved => Log.info('Checklist saved', this, saved))
      );
  }

  private checklist(): Observable<ProgrammeChecklistDetailDTO> {
    const initialChecklist$ =  this.checkListId()
      .pipe(
        switchMap(checklistId => checklistId
          ? this.programmeChecklistService.getProgrammeChecklistDetail(checklistId as number)
          : of({} as ProgrammeChecklistDetailDTO)
        ),
        tap(checklist => Log.info('Fetched checklist', this, checklist))
      );

    return merge(initialChecklist$, this.savedChecklist$);
  }


  private checkListId(): Observable<any> {
    return this.routingService.routeParameterChanges(ProgrammeChecklistDetailPageStore.CHECKLIST_DETAIL_PATH, 'checklistId');
  }

  private isEditable(): Observable<boolean> {
    return combineLatest([this.programmeEditableStateStore.hasEditPermission$, this.checklist$])
      .pipe(
        map(([hasEditPermission, checklist]) => hasEditPermission && !checklist.locked),
      );
  }
}
