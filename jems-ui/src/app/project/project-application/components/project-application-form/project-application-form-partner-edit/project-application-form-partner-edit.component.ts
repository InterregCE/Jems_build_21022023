import {
  ChangeDetectionStrategy,
  Component
} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {
  InputProjectPartnerCreate,
  InputProjectPartnerUpdate,
  InputTranslation,
  OutputProjectPartner,
  OutputProjectPartnerDetail,
  ProgrammeLegalStatusService,
} from '@cat/api';
import {catchError, take, tap} from 'rxjs/operators';
import {MatDialog} from '@angular/material/dialog';
import {Forms} from '@common/utils/forms';
import {FormService} from '@common/components/section/form/form.service';
import {ProjectPartnerStore} from '../../../containers/project-application-form-page/services/project-partner-store.service';
import {HttpErrorResponse} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {APIError} from '@common/models/APIError';
import {APPLICATION_FORM} from '@project/application-form-model';
import {Tools} from '@common/utils/tools';
import {RoutingService} from '@common/services/routing.service';

@Component({
  selector: 'app-project-application-form-partner-edit',
  templateUrl: './project-application-form-partner-edit.component.html',
  styleUrls: ['./project-application-form-partner-edit.component.scss'],
  providers: [FormService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFormPartnerEditComponent {
  RoleEnum = OutputProjectPartner.RoleEnum;
  VatRecoveryEnum = InputProjectPartnerCreate.VatRecoveryEnum;
  LANGUAGE = InputTranslation.LanguageEnum;
  APPLICATION_FORM = APPLICATION_FORM;

  partnerId = this.router.getParameter(this.activatedRoute, 'partnerId');
  partner$: Observable<OutputProjectPartnerDetail>;
  legalStatuses$ = this.programmeLegalStatusService.getProgrammeLegalStatusList();

  partnerForm: FormGroup = this.formBuilder.group({
    fakeRole: [], // needed for the fake role field in view mode
    id: [],
    sortNumber: [],
    abbreviation: ['', [
      Validators.maxLength(15),
      Validators.required]
    ],
    role: ['', Validators.required],
    nameInOriginalLanguage: ['', Validators.maxLength(100)],
    nameInEnglish: [[], Validators.maxLength(100)],
    department: [],
    partnerType: [''],
    legalStatusId: ['', Validators.required],
    vat: ['', Validators.maxLength(50)],
    vatRecovery: ['']
  });

  roleErrors = {
    required: 'project.partner.role.should.not.be.empty',
  };
  legalStatusErrors = {
    required: 'project.partner.legal.status.should.not.be.empty'
  };

  partnerTypeEnums = [
    'LocalPublicAuthority',
    'RegionalPublicAuthority',
    'NationalPublicAuthority',
    'SectoralAgency',
    'InfrastructureAndServiceProvider',
    'InterestGroups',
    'HigherEducationOrganisations',
    'EducationTrainingCentreAndSchool',
    'EnterpriseExceptSme',
    'Sme',
    'BusinessSupportOrganisation',
    'Egtc',
    'InternationalOrganisationEeig',
    'GeneralPublic',
    'Hospitals',
    'Other'
  ];

  constructor(private formBuilder: FormBuilder,
              private dialog: MatDialog,
              public formService: FormService,
              private partnerStore: ProjectPartnerStore,
              private router: RoutingService,
              private activatedRoute: ActivatedRoute,
              private programmeLegalStatusService: ProgrammeLegalStatusService) {
    this.formService.init(this.partnerForm, this.partnerStore.isProjectEditable$);
    this.partner$ = this.partnerStore.partner$
      .pipe(
        tap(partner => this.resetForm(partner))
      );
  }

  get controls(): any {
    return this.partnerForm.controls;
  }

  onSubmit(controls: any, oldPartnerId?: number): void {
    if (!controls.id?.value) {
      const partnerToCreate = {
        abbreviation: this.controls.abbreviation.value,
        role: this.controls.role.value,
        oldLeadPartnerId: oldPartnerId,
        nameInOriginalLanguage: this.controls.nameInOriginalLanguage.value,
        nameInEnglish: this.controls.nameInEnglish.value[0].translation,
        department: this.controls.department.value,
        partnerType: this.controls.partnerType.value,
        legalStatusId: this.controls.legalStatusId.value,
        vat: this.controls.vat.value,
        vatRecovery: this.controls.vatRecovery.value,
      };

      partnerToCreate.oldLeadPartnerId = oldPartnerId;
      if (!controls.partnerType.value) {
        partnerToCreate.partnerType = null;
      }

      this.partnerStore.createPartner(partnerToCreate as InputProjectPartnerCreate)
        .pipe(
          take(1),
          tap(created => this.redirectToPartnerDetail(created)),
          catchError(error => this.handleError(error))
        ).subscribe();
    } else {
      const partnerToUpdate = {
        id: this.controls.id.value,
        abbreviation: this.controls.abbreviation.value,
        role: this.controls.role.value,
        oldLeadPartnerId: oldPartnerId,
        nameInOriginalLanguage: this.controls.nameInOriginalLanguage.value,
        nameInEnglish: this.controls.nameInEnglish.value[0].translation,
        department: this.controls.department.value,
        partnerType: this.controls.partnerType.value,
        legalStatusId: this.controls.legalStatusId.value,
        vat: this.controls.vat.value,
        vatRecovery: this.controls.vatRecovery.value,
      };

      partnerToUpdate.oldLeadPartnerId = oldPartnerId;
      if (!controls.partnerType.value) {
        partnerToUpdate.partnerType = null;
      }
      this.partnerStore.savePartner(partnerToUpdate as InputProjectPartnerUpdate)
        .pipe(
          take(1),
          tap(() => this.formService.setSuccess('project.partner.save.success')),
          catchError(error => this.handleError(error))
        ).subscribe();
    }
  }

  discard(partner?: OutputProjectPartnerDetail): void {
    if (!this.partnerId) {
      this.redirectToPartnerOverview();
    } else {
      this.resetForm(partner);
    }
  }

  private handleError(error: HttpErrorResponse): Observable<any> {
    const errorMessage = Tools.first((error?.error as APIError)?.details)?.i18nMessage;
    if (errorMessage?.i18nKey === 'use.case.update.project.partner.role.lead.already.existing'
      || errorMessage?.i18nKey === 'use.case.create.project.partner.role.lead.already.existing') {
      this.handleLeadAlreadyExisting(this.controls, errorMessage.i18nArguments);
      return of(null);
    }
    return this.formService.setError(error);
  }

  private resetForm(partner?: OutputProjectPartnerDetail): void {
    if (!this.partnerId) {
      this.formService.setCreation(true);
    }
    this.controls.id.setValue(partner?.id);
    this.controls.abbreviation.setValue(partner?.abbreviation);
    this.controls.role.setValue(partner?.role);
    this.controls.nameInOriginalLanguage.setValue(partner?.nameInOriginalLanguage);
    this.controls.nameInEnglish.setValue([{
      language: this.LANGUAGE.EN,
      translation: partner?.nameInEnglish || ''
    }]);
    this.controls.department.setValue(partner?.department);
    this.controls.partnerType.setValue(partner?.partnerType);
    this.controls.legalStatusId.setValue(partner?.legalStatusId);
    this.controls.vat.setValue(partner?.vat);
    this.controls.vatRecovery.setValue(partner?.vatRecovery);
    this.controls.sortNumber.setValue(partner?.sortNumber);
  }

  private handleLeadAlreadyExisting(controls: any, errorArgs: { [key: string]: string; }): void {
    Forms.confirmDialog(
      this.dialog,
      'project.partner.role.lead.already.existing.title',
      'project.partner.role.lead.already.existing',
      {
        old_name: errorArgs.currentLeadAbbreviation,
        new_name: controls.abbreviation.value
      }
    ).pipe(
      take(1),
    ).subscribe(change => {
      if (change) {
        this.onSubmit(controls, errorArgs.currentLeadId as any);
      } else {
        this.formService.setDirty(true);
      }
    });
  }

  private redirectToPartnerOverview(): void {
    this.router.navigate(['..'], {relativeTo: this.activatedRoute});
  }

  private redirectToPartnerDetail(partner: any): void {
    this.router.navigate(
      ['..', partner.id, 'identity'],
      {relativeTo: this.activatedRoute}
    );
  }

}
