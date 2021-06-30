import {ChangeDetectionStrategy, Component} from '@angular/core';
import {combineLatest, Observable} from 'rxjs';
import {AbstractControl, FormArray, FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {MatTreeFlatDataSource, MatTreeFlattener} from '@angular/material/tree';
import {FlatTreeControl} from '@angular/cdk/tree';
import {FlatTreeNode} from '@common/models/flat-tree-node';
import {ApplicationFormConfigurationPageStore} from './application-form-configuration-page-store.service';
import {FormService} from '@common/components/section/form/form.service';
import {catchError, map, tap} from 'rxjs/operators';
import {ApplicationFormFieldNode} from './application-form-field-node';
import {CallPageSidenavService} from '../services/call-page-sidenav.service';
import AvailableInStepEnum = ApplicationFormFieldConfigurationDTO.AvailableInStepEnum;
import {ApplicationFormFieldConfigurationDTO} from '@cat/api';
import {take} from 'rxjs/internal/operators';
import {Alert} from '@common/components/forms/alert';

@Component({
  selector: 'app-application-form-configuration-page',
  templateUrl: './application-form-configuration-page.component.html',
  styleUrls: ['./application-form-configuration-page.component.scss'],
  providers: [FormService, ApplicationFormConfigurationPageStore],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationFormConfigurationPageComponent {
  AvailableInStepEnum = AvailableInStepEnum;
  Alert = Alert;
  displayedColumns: string[];

  data$: Observable<{
    fieldConfigurations: ApplicationFormFieldNode[],
    callHasTwoSteps: boolean
  }>;

  form = this.formBuilder.group({
    formFields: this.formBuilder.array([])
  });

  treeControl: FlatTreeControl<FlatTreeNode<AbstractControl>>;
  dataSource: MatTreeFlatDataSource<ApplicationFormFieldNode, FlatTreeNode<AbstractControl>>;

  constructor(private pageStore: ApplicationFormConfigurationPageStore,
              private formService: FormService,
              private formBuilder: FormBuilder,
              private callSidenavService: CallPageSidenavService) {
    this.formService.init(this.form);
    this.initializeDataSource();
    this.data$ = combineLatest([this.pageStore.fieldConfigurations$, this.pageStore.callHasTwoSteps$])
      .pipe(
        map(([fieldConfigurations, callHasTwoSteps]) => ({fieldConfigurations, callHasTwoSteps})),
        tap(data => this.displayedColumns = data.callHasTwoSteps ? ['name', 'show', 'step'] : ['name', 'show']),
        tap(data => this.resetForm(data.fieldConfigurations))
      );
  }

  resetForm(configs: ApplicationFormFieldNode[]): void {
    this.formFields.clear();
    this.dataSource.data = configs;
    this.treeControl.expandAll();
  }

  save(): void {
    this.pageStore.saveConfigurations(this.formFields.getRawValue())
      .pipe(
        take(1),
        tap(() => this.formService.setSuccess('call.detail.application.form.config.saved.success')),
        catchError(error => this.formService.setError(error))
      ).subscribe();
  }

  get formFields(): FormArray {
    return this.form.get('formFields') as FormArray;
  }

  availableInStep(group: FormGroup): FormControl {
    return group.get('availableInStep') as FormControl;
  }

  isVisible(group: FormGroup): FormControl {
    return group.get('isVisible') as FormControl;
  }

  visibilityChanged(control: FormGroup): void {
    const availableInStep = this.availableInStep(control);
    if (!this.isVisible(control)?.value) {
      availableInStep?.disable();
      availableInStep?.setValue(AvailableInStepEnum.NONE);
    } else {
      availableInStep?.enable();
      availableInStep?.setValue(AvailableInStepEnum.STEPTWOONLY);
    }
  }

  private initializeDataSource(): void {
    this.treeControl = new FlatTreeControl<FlatTreeNode<AbstractControl>>(
      node => node.level, node => node.expandable);
    const treeFlattener = new MatTreeFlattener(
      (node: ApplicationFormFieldNode, level: number) => ({
        expandable: !!node.children?.length,
        level,
        parentIndex: node.parentIndex,
        data: this.createControl(node)
      }),
      node => node.level,
      node => node.expandable,
      node => node.children
    );
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, treeFlattener);
  }

  private createControl(node: ApplicationFormFieldNode): FormGroup {
    const control = this.formBuilder.group({
      id: node.id,
      isVisible: this.formBuilder.control({
        value: node.isVisible,
        disabled: node.isVisibilityLocked
      }),
      availableInStep: this.formBuilder.control({
        value: node.availableInStep,
        disabled: node.isStepSelectionLocked
      })
    });
    if (node.availableInStep) {
      this.formFields.push(control);
    }
    return control;
  }
}
