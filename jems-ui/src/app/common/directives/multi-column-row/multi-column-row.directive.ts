import {Directive, ElementRef, Input, OnInit} from '@angular/core';

@Directive({
  selector: '[jemsMultiColumnRow]'
})
export class MultiColumnRowDirective implements OnInit {
  @Input() gap = '1rem';
  @Input() stretch = 'none';
  @Input() justifyContent: 'start' | 'space-between' = 'start';
  @Input() alignItems: 'start' | 'end' | 'center' = 'center';
  @Input() display = 'flex';

  constructor(private el: ElementRef) {
  }

  ngOnInit(): void {
    this.el.nativeElement.classList.add('jems-layout');
    this.el.nativeElement.classList.add('jems-layout-row');
    this.el.nativeElement.classList.add('jems-multi-column-row');
    this.el.nativeElement.style.setProperty('--column-gap', this.gap);
    this.el.nativeElement.style.setProperty('--flex', this.stretch === 'none' ? 'none' : 1);
    this.el.nativeElement.style.setProperty('--justifyContent', this.justifyContent);
    this.el.nativeElement.style.setProperty('--alignItems', this.alignItems);
    this.el.nativeElement.style.setProperty('display', this.display);
  }
}
