import {Injectable} from '@angular/core';
import {
  ProjectBudgetOverviewPerPartnerPerPeriodDTO,
  ProjectBudgetService, ProjectFundsPerPeriodDTO,
  ProjectFundsService,
} from '@cat/api';
import {ProjectVersionStore} from '@project/common/services/project-version-store.service';
import {ProjectStore} from '@project/project-application/containers/project-application-detail/services/project-store.service';
import {combineLatest, Observable} from 'rxjs';
import {startWith, switchMap} from 'rxjs/operators';
import {ProjectPartnerBudgetStore} from '@project/budget/services/project-partner-budget.store';
import {ProjectPartnerStore} from '@project/project-application/containers/project-application-form-page/services/project-partner-store.service';
import {ProjectLumpSumsStore} from '@project/lump-sums/project-lump-sums-page/project-lump-sums-store.service';
import {ProjectPartnerCoFinancingStore} from '@project/partner/project-partner-detail-page/project-partner-co-financing-tab/services/project-partner-co-financing.store';


@Injectable()
export class ProjectBudgetPeriodPageStore {

  projectBudgetFundsPerPeriod$: Observable<ProjectFundsPerPeriodDTO>;
  projectBudgetOverviewPerPartnerPerPeriods$ = new Observable<ProjectBudgetOverviewPerPartnerPerPeriodDTO>();

  projectTitle$ = this.projectStore.projectTitle$;
  projectPeriods$ = this.projectStore.projectPeriods$;

  constructor(private projectStore: ProjectStore,
              private projectVersionStore: ProjectVersionStore,
              private projectFundsService: ProjectFundsService,
              private projectBudgetService: ProjectBudgetService,
              private projectPartnerBudgetStore: ProjectPartnerBudgetStore,
              private projectPartnerStore: ProjectPartnerStore,
              private projectLumpSumsPageStore: ProjectLumpSumsStore,
              private projectPartnerCoFinancingStore: ProjectPartnerCoFinancingStore) {
    this.projectBudgetFundsPerPeriod$ = this.projectBudgetFundsPerPeriod();
    this.projectBudgetOverviewPerPartnerPerPeriods$ = this.projectPartnersBudgetPerPeriods();
  }

  private projectBudgetFundsPerPeriod(): Observable<ProjectFundsPerPeriodDTO> {
    return combineLatest([
      this.projectStore.projectId$,
      this.projectVersionStore.selectedVersionParam$,
      this.projectPartnerBudgetStore.budgets$.pipe(startWith(null)),
      this.projectPartnerStore.partner$,
      this.projectPartnerStore.partnerSummaries$,
      this.projectLumpSumsPageStore.projectLumpSums$,
      this.projectPartnerCoFinancingStore.financingAndContribution$.pipe(startWith(null))
    ]).pipe(
      switchMap(([projectId, selectedVersion]: any) => this.projectFundsService.getProjectBudgetFundsPerPeriod(projectId, selectedVersion))
    );
  }

  private projectPartnersBudgetPerPeriods(): Observable<ProjectBudgetOverviewPerPartnerPerPeriodDTO> {
    return combineLatest([
      this.projectStore.project$,
      this.projectVersionStore.selectedVersionParam$,
      this.projectPartnerBudgetStore.budgets$.pipe(startWith(null)),
      this.projectPartnerStore.partner$,
      this.projectPartnerStore.partnerSummaries$,
      this.projectLumpSumsPageStore.projectLumpSums$
    ])
      .pipe(
        switchMap(([project, version]) =>
          this.projectBudgetService.getProjectPartnerBudgetPerPeriod(project.id, version)
        )
      );
  }
}
