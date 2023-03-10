import {Directive, ElementRef, Input, OnInit} from '@angular/core';

type WidthType = 'small' | 'medium' | 'large' | 'x-large' | 'xx-large' | 'xxx-large' | 'half' | 'full' | 'one-third' | 'two-thirds' | 'one-quarter';
type NumberOfCharsType = 1 | 2 | 3 | 4 | 5;

@Directive({
  selector: '[jemsFormFieldWidth]'
})
export class FormFieldWidthDirective implements OnInit {
  @Input('jemsFormFieldWidth') formFieldWidth: WidthType;
  @Input() expectedNumberOfChars: NumberOfCharsType;
  @Input() extendError: true = true;
  @Input() minWidth: string;
  @Input() maxWidth: string;

  constructor(private el: ElementRef) {
  }

  ngOnInit(): void {
    this.el.nativeElement.classList.add(`jems-layout`);
    this.el.nativeElement.classList.add(`jems-form-field`);
    this.el.nativeElement.classList.add(`jems-form-field-width`);
    this.el.nativeElement.style.setProperty('--widthBasedOnType', this.mapWidthToActualWidth(this.formFieldWidth));

    if (this.expectedNumberOfChars) {
      this.el.nativeElement.style.setProperty('--widthBasedOnNumberChars', this.mapNumberOfCharsToRem(this.expectedNumberOfChars));
    }
    if (this.maxWidth) {
      this.el.nativeElement.style.maxWidth = this.maxWidth;
    }
    if (this.minWidth) {
      this.el.nativeElement.style.minWidth = this.minWidth;
    }

  }

  private mapNumberOfCharsToRem(numberOfChars: number): string {
    switch (numberOfChars) {
      case 1:
        return '1.5rem';
      case 2:
        return '2rem';
      case 3:
        return '2.5rem';
      case 4:
        return '3rem';
      case 5:
        return '3.5rem';
      default:
        return '';
    }
  }

  private mapWidthToActualWidth(width: string): string {
    switch (width) {
      case 'small':
        return '5rem';
      case 'medium':
        return '7.5rem';
      case 'large':
        return '10rem';
      case 'x-large':
        return '15rem';
      case 'xx-large':
        return '20rem';
      case 'xxx-large':
        return '25rem';
      case 'half':
        return '50%';
      case 'full':
        return '100%';
      case 'one-third':
          return '33%';
      case 'two-thirds':
          return '66%';
      case 'one-quarter':
        return '25%';
      default:
        return width;
    }
  }
}
