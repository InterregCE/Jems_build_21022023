import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {map} from 'rxjs/operators';
import {ProjectFundingDecisionStore} from './project-funding-decision-store.service';
import {ProjectStore} from '../../project-application/containers/project-application-detail/services/project-store.service';
import {combineLatest} from 'rxjs';
import {ProjectStepStatus} from '../project-step-status';
import {ProjectStatusDTO, UserRoleDTO} from '@cat/api';
import {PermissionService} from '../../../security/permissions/permission.service';
import StatusEnum = ProjectStatusDTO.StatusEnum;
import Permissions = UserRoleDTO.PermissionsEnum;

@Component({
  selector: 'jems-project-application-funding-page',
  templateUrl: './project-application-funding-page.component.html',
  styleUrls: ['./project-application-funding-page.component.scss'],
  providers: [ProjectFundingDecisionStore],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFundingPageComponent {

  projectId = this.activatedRoute.snapshot.params.projectId;
  step = this.activatedRoute.snapshot.params.step;
  stepStatus = new ProjectStepStatus(this.step);

  details$ = combineLatest([
    this.fundingDecisionStore.currentVersionOfProject$,
    this.fundingDecisionStore.currentVersionOfProjectTitle$,
    this.fundingDecisionStore.preFundingDecision(this.step),
    this.fundingDecisionStore.finalFundingDecision(this.step),
    this.fundingDecisionStore.eligibilityDecisionDate(this.step),
    this.permissionService.hasPermission([Permissions.ProjectStatusDecideApproved, Permissions.ProjectStatusDecideNotApproved]),
  ])
    .pipe(
      map(([currentVersionOfProject,currentVersionOfProjectTitle, preFundingDecision, finalFundingDecision, eligibilityDecisionDate, userCanChangeFunding]) => ({
        currentVersionOfProject,
        currentVersionOfProjectTitle,
        preFundingDecision,
        finalFundingDecision,
        eligibilityDecisionDate,
        showSecondDecision: !!currentVersionOfProject.secondStepDecision?.preFundingDecision
          && currentVersionOfProject.projectStatus.status !== StatusEnum.RETURNEDTOAPPLICANT
          && currentVersionOfProject.projectStatus.status !== StatusEnum.RETURNEDTOAPPLICANTFORCONDITIONS
          && (!!finalFundingDecision || userCanChangeFunding),
        fullOptions: [this.stepStatus.approved, this.stepStatus.approvedWithConditions, this.stepStatus.notApproved],
        optionsForSecondDecision: [this.stepStatus.approved, this.stepStatus.notApproved],
        userCanChangeFunding,
      })),
    )
  ;

  constructor(public projectStore: ProjectStore,
              private router: Router,
              private activatedRoute: ActivatedRoute,
              private permissionService: PermissionService,
              public fundingDecisionStore: ProjectFundingDecisionStore) {
  }

}
