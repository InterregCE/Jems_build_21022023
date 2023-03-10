import {Directive, ElementRef, OnInit} from '@angular/core';

@Directive({
  selector: '[jemsNoWidthLimit]'
})
export class NoWidthLimitDirective implements OnInit {
  constructor(private el: ElementRef) {
  }

  ngOnInit(): void {
    this.el.nativeElement.style.maxWidth = '100%';
  }
}
