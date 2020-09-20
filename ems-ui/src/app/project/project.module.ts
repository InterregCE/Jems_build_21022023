import {NgModule} from '@angular/core';
import {DatePipe} from '@angular/common';
import {ProjectRoutingModule} from './project-routing.module';
import {CoreModule} from '../common/core-module';
import {ProjectApplicationDetailComponent} from './project-application/containers/project-application-detail/project-application-detail.component';
import {ProjectApplicationSubmissionComponent} from './project-application/components/project-application-submission/project-application-submission.component';
import {ProjectApplicationListComponent} from './project-application/components/project-application-list/project-application-list.component';
import {ProjectApplicationComponent} from './project-application/containers/project-application-page/project-application.component';
import {SharedModule} from '../common/shared-module';
import {OverlayModule} from '@angular/cdk/overlay';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import {MatListModule} from '@angular/material/list';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ProjectApplicationDataComponent} from './project-application/containers/project-application-detail/project-application-data/project-application-data.component';
import {ProjectApplicationFilesListComponent} from './project-application/components/project-application-detail/project-application-files-list/project-application-files-list.component';
import {ProjectApplicationInformationComponent} from './project-application/components/project-application-detail/project-application-information/project-application-information.component';
import {ProjectApplicationFileUploadComponent} from './project-application/components/project-application-detail/project-application-file-upload/project-application-file-upload.component';
import {ProjectApplicationAssessmentsComponent} from './project-application/components/project-application-detail/project-application-assessments/project-application-assessments.component';
import {ProjectApplicationFilesComponent} from './project-application/containers/project-application-detail/project-application-files/project-application-files.component';
import {DescriptionCellComponent} from './project-application/components/project-application-detail/project-application-files-list/cell-renderers/description-cell/description-cell.component';
import {ProjectApplicationEligibilityDecisionComponent} from './project-application/components/project-application-detail/project-application-eligibility-decision/project-application-eligibility-decision.component';
import {MatRadioModule} from '@angular/material/radio';
import {ProjectApplicationEligibilityCheckComponent} from './project-application/components/project-application-detail/project-application-eligibility-check/project-application-eligibility-check.component';
import {ProjectApplicationQualityCheckComponent} from './project-application/components/project-application-detail/project-application-quality-check/project-application-quality-check.component';
import {ProjectStore} from './project-application/containers/project-application-detail/services/project-store.service';
import {MatExpansionModule} from '@angular/material/expansion';
import {ActionsCellComponent} from './project-application/components/project-application-detail/project-application-files-list/cell-renderers/actions-cell/actions-cell.component';
import {ProjectApplicationDecisionsComponent} from './project-application/components/project-application-detail/project-application-decisions/project-application-decisions.component';
import {ProjectApplicationActionsComponent} from './project-application/components/project-application-detail/project-application-actions/project-application-actions.component';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MAT_MOMENT_DATE_ADAPTER_OPTIONS, MatMomentDateModule} from '@angular/material-moment-adapter';
import {MAT_DATE_LOCALE} from '@angular/material/core';
import {ProjectApplicationFundingPageComponent} from './project-application/containers/project-application-detail/project-application-funding-page/project-application-funding-page.component';
import {ProjectApplicationFundingDecisionComponent} from './project-application/components/project-application-detail/project-application-funding-decision/project-application-funding-decision.component';
import {ProjectApplicationEligibilityDecisionPageComponent} from './project-application/containers/project-application-detail/project-application-eligibility-decision-page/project-application-eligibility-decision-page.component';
import {ProjectApplicationFormPageComponent} from './project-application/containers/project-application-form-page/project-application-form-page.component';
import {MatSidenavModule} from '@angular/material/sidenav';
import {ProjectApplicationFormComponent} from './project-application/components/project-application-form/project-application-form.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ProjectApplicationFormPolicyRadioButtonComponent} from './project-application/components/project-application-form/project-application-form-policy-radio-button/project-application-form-policy-radio-button.component';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {ProjectApplicationFormWorkPackagesListComponent} from './project-application/components/project-application-form/project-application-form-work-packages-list/project-application-form-work-packages-list.component';
import {ProjectApplicationFormWorkPackageDetailComponent} from './project-application/components/project-application-form/project-application-form-work-package-detail/project-application-form-work-package-detail.component';
import {WorkPackageDetailsComponent} from './project-application/containers/project-application-form-page/project-application-form-work-package-section/work-package-details/work-package-details.component';
import {ProjectApplicationFormWorkPackageSectionComponent} from './project-application/containers/project-application-form-page/project-application-form-work-package-section/project-application-form-work-package-section.component';
import {ProjectApplicationFormPartnerSectionComponent} from './project-application/containers/project-application-form-page/project-application-form-partner-section/project-application-form-partner-section.component';
import {ProjectApplicationFormPartnerListComponent} from './project-application/components/project-application-form/project-application-form-partner-list/project-application-form-partner-list.component';
import {ProjectApplicationFormPartnerDetailComponent} from './project-application/containers/project-application-form-page/project-application-form-partner-section/project-application-form-partner-detail/project-application-form-partner-detail.component';
import {ProjectApplicationFormPartnerEditComponent} from './project-application/components/project-application-form/project-application-form-partner-edit/project-application-form-partner-edit.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {ProjectApplicationFormSidenavService} from './project-application/containers/project-application-form-page/services/project-application-form-sidenav.service';
import {ProjectApplicationFormManagementSectionComponent} from './project-application/containers/project-application-form-page/project-application-form-management-section/project-application-form-management-section.component';
import {ProjectApplicationFormFuturePlansSectionComponent} from './project-application/containers/project-application-form-page/project-application-form-future-plans-section/project-application-form-future-plans-section.component';
import {ProjectApplicationFormManagementDetailComponent} from './project-application/components/project-application-form/project-application-form-management-detail/project-application-form-management-detail.component';
import {ProjectApplicationFormFuturePlansDetailComponent} from './project-application/components/project-application-form/project-application-form-future-plans-detail/project-application-form-future-plans-detail.component';
import {ContributionRadioColumnComponent} from './project-application/components/project-application-form/project-application-form-management-detail/contribution-radio-column/contribution-radio-column.component';
import {ProjectApplicationFormPartnerContactComponent} from './project-application/components/project-application-form/project-application-form-partner-contact/project-application-form-partner-contact.component';
import {ProjectAcronymBreadcrumbProvider} from './project-application/containers/project-application-detail/services/project-acronym-breadcrumb-provider.guard';

@NgModule({
  declarations: [
    DescriptionCellComponent,
    ProjectApplicationComponent,
    ProjectApplicationListComponent,
    ProjectApplicationSubmissionComponent,
    ProjectApplicationDetailComponent,
    ProjectApplicationDataComponent,
    ProjectApplicationFilesListComponent,
    ProjectApplicationInformationComponent,
    ProjectApplicationFileUploadComponent,
    ProjectApplicationAssessmentsComponent,
    ProjectApplicationFilesComponent,
    ProjectApplicationDecisionsComponent,
    ProjectApplicationActionsComponent,
    ProjectApplicationEligibilityDecisionComponent,
    ProjectApplicationQualityCheckComponent,
    ProjectApplicationEligibilityCheckComponent,
    ActionsCellComponent,
    ProjectApplicationFundingPageComponent,
    ProjectApplicationFundingDecisionComponent,
    ProjectApplicationEligibilityDecisionPageComponent,
    ProjectApplicationFormPageComponent,
    ProjectApplicationFormComponent,
    ProjectApplicationFormPolicyRadioButtonComponent,
    ProjectApplicationFormPartnerSectionComponent,
    ProjectApplicationFormPartnerListComponent,
    ProjectApplicationFormPartnerDetailComponent,
    ProjectApplicationFormPartnerEditComponent,
    ProjectApplicationFormWorkPackagesListComponent,
    ProjectApplicationFormWorkPackageDetailComponent,
    WorkPackageDetailsComponent,
    ProjectApplicationFormWorkPackageSectionComponent,
    ProjectApplicationFormManagementSectionComponent,
    ProjectApplicationFormFuturePlansSectionComponent,
    ProjectApplicationFormManagementDetailComponent,
    ProjectApplicationFormFuturePlansDetailComponent,
    ContributionRadioColumnComponent,
    ProjectApplicationFormWorkPackageSectionComponent,
    ProjectApplicationFormPartnerContactComponent
  ],
  imports: [
    SharedModule,
    CoreModule,
    ProjectRoutingModule,
    MatListModule,
    MatSelectModule,
    MatDialogModule,
    OverlayModule,
    BrowserAnimationsModule,
    MatRadioModule,
    MatExpansionModule,
    MatDatepickerModule,
    MatMomentDateModule,
    MatSidenavModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatButtonToggleModule,
  ],
  exports: [
    ProjectApplicationListComponent,
    ProjectApplicationDetailComponent
  ],
  providers: [
    DatePipe,
    ProjectStore,
    ProjectApplicationFormSidenavService,
    MatDatepickerModule,
    ProjectAcronymBreadcrumbProvider,
    {provide: MAT_MOMENT_DATE_ADAPTER_OPTIONS, useValue: {useUtc: true}},
    {provide: MAT_DATE_LOCALE, useValue: 'en-GB'}
  ]
})
export class ProjectModule {
}
