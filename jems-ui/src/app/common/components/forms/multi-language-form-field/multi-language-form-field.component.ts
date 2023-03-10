import {Component, ElementRef, forwardRef, Input, OnInit, ViewChild} from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator,
  Validators
} from '@angular/forms';
import {InputTranslation} from '@cat/api';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {MultiLanguageFormFieldConstants} from '@common/components/forms/multi-language-form-field/multi-language-form-field.constants';
import {map, tap} from 'rxjs/operators';
import {Observable, ReplaySubject} from 'rxjs';
import {MultiLanguageContainerService} from '@common/components/forms/multi-language-container/multi-language-container.service';
import {INPUT_STATE} from '@common/components/forms/multi-language-container/multi-language-input-state';
import {LanguageStore} from '@common/services/language-store.service';

@UntilDestroy()
@Component({
  selector: 'jems-multi-language-form-field',
  templateUrl: './multi-language-form-field.component.html',
  styleUrls: ['./multi-language-form-field.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MultiLanguageFormFieldComponent),
      multi: true
    },
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => MultiLanguageFormFieldComponent),
      multi: true
    }
  ]
})
export class MultiLanguageFormFieldComponent implements OnInit, ControlValueAccessor, Validator {

  constants = MultiLanguageFormFieldConstants;

  @Input()
  type: 'input' | 'textarea' = 'input';
  @Input()
  label: string;
  @Input()
  maxLength = 255;
  @Input()
  minRows ? = 3;
  @Input()
  maxRows ? = 15;
  @Input()
  contextInfoText?: string;
  @Input()
  useFallBackLanguage = false;
  @Input()
  isFallBackLanguageReadonly = false;
  @Input()
  disabled = false;
  @Input()
  condensed = false;
  @Input()
  isRequired = false;
  @Input()
  hasFocus = false;

  multiLanguageFormGroup: FormGroup;
  currentLanguage$: Observable<string>;
  state$ = new ReplaySubject<{ [key: string]: INPUT_STATE }>(1);
  @ViewChild('input') inputRef: ElementRef;

  constructor(private multiLanguageContainerService: MultiLanguageContainerService,
              public languageStore: LanguageStore,
              public formBuilder: FormBuilder) {
  }

  ngOnInit(): void {
    this.multiLanguageContainerService.languages$.pipe(
      map((languages => this.useFallBackLanguage && languages.indexOf(this.languageStore.getFallbackLanguageValue()) < 0 ?
        [...languages, this.languageStore.getFallbackLanguageValue()] : [...languages])),
      tap(languages => this.resetForm(languages)),
      untilDestroyed(this)
    ).subscribe();

    this.currentLanguage$ = this.multiLanguageContainerService.activeLanguage$;
    if (this.hasFocus) {
      setTimeout(() => this.inputRef.nativeElement.focus(), 50);
    }
  }

  registerOnChange(fn: any): void {
    this.inputs?.valueChanges.pipe(
      untilDestroyed(this),
      tap(() => this.valueChanged()),
    ).subscribe(fn);
  }

  registerOnTouched(fn: any): void {
    // This is intentional
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  writeValue(newValue: InputTranslation[]): void {
    if (this.multiLanguageFormGroup) {
      if (newValue && Array.isArray(newValue)) {
        if (this.multiLanguageContainerService.didLanguagesChange(newValue)) {
          const inputTranslations = this.multiLanguageContainerService.multiLanguageFormFieldDefaultValue(this.useFallBackLanguage);
          inputTranslations.forEach(defaultItem => {
            defaultItem.translation = newValue.find(it => it.language === defaultItem.language)?.translation || '';
          });
          this.inputs.setValue(inputTranslations, {emitEvent: false});
        } else {
          this.inputs.patchValue(newValue, {emitEvent: false});
        }
      } else {
        this.inputs.setValue(this.multiLanguageContainerService.multiLanguageFormFieldDefaultValue(this.useFallBackLanguage), {emitEvent: false});
      }
      this.valueChanged();
    }
  }

  registerOnValidatorChange(fn: () => void): void {
    // This is intentional
  }

  validate(control: AbstractControl): ValidationErrors | null {
    this.multiLanguageFormGroup?.updateValueAndValidity();
    return this.multiLanguageFormGroup?.valid ? null : {
      multiLanguageError: {
        valid: false,
        message: 'multiLanguage fields are invalid'
      }
    };
  }

  isEditable(language: string, fallbackLanguage: string | null): boolean {
    return !(this.disabled || (this.isFallBackLanguageReadonly && language === fallbackLanguage));
  }

  getLanguageValue(formGroup: AbstractControl): string {
    return formGroup.get(this.constants.FORM_CONTROL_NAMES.language)?.value;
  }

  getTranslation(formGroup: AbstractControl): FormControl {
    return formGroup.get(this.constants.FORM_CONTROL_NAMES.translation) as FormControl;
  }

  getTranslationLength(formGroup: AbstractControl): number {
    return this.getTranslation(formGroup)?.value?.length ? this.getTranslation(formGroup)?.value?.length : 0;
  }

  private initForm(languages: string[]): void {
    this.multiLanguageFormGroup = this.formBuilder.group({
      inputs: this.formBuilder.array([])
    });
    languages.forEach(language => {
        this.inputs.push(
          this.formBuilder.group({
            translation: ['', this.isRequired ? Validators.compose([Validators.maxLength(this.maxLength), Validators.required]) : Validators.maxLength(this.maxLength)],
            language: [language]
          }));
      }
    );
  }

  private resetForm(languages: string[]): void {
    if (!this.multiLanguageFormGroup) {
      this.initForm(languages);
    } else {
      const finalFromGroups: FormGroup[] = [];
      languages.forEach(language => {
          const formGroupIndexOfIndex = this.getIndexOfFormGroupForLanguage(language);
          if (formGroupIndexOfIndex >= 0) {
            finalFromGroups.push(this.inputs.controls[formGroupIndexOfIndex] as FormGroup);
          } else {
            finalFromGroups.push(
              this.formBuilder.group({
                translation: ['', Validators.maxLength(this.maxLength)],
                language: language.valueOf
              }));
          }
        }
      );
      this.inputs.clear();
      finalFromGroups.forEach(formGroup => {
        this.inputs.push(formGroup);
      });
    }
  }

  private valueChanged(): void {
    this.state$.next(
      Object.fromEntries(
        this.inputs?.controls.map(group => {
          const translation = this.getTranslation(group);
          const language = this.getLanguageValue(group);
          if (translation?.invalid) {
            return [language, INPUT_STATE.INVALID];
          }
          return [language, translation?.value || this.disabled ? INPUT_STATE.VALID : INPUT_STATE.EMPTY];
        })
      ));
  }

  private getIndexOfFormGroupForLanguage(language: string): number {
    return this.inputs.controls.findIndex(formGroup => this.getLanguageValue(formGroup) === language);
  }

  get inputs(): FormArray {
    return this.multiLanguageFormGroup?.get(this.constants.FORM_CONTROL_NAMES.inputs) as FormArray;
  }
}
