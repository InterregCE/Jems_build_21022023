import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {CallDetailDTO, CallService, UserRoleDTO} from '@cat/api';
import {Router} from '@angular/router';
import {CallStore} from '../../services/call-store.service';
import PermissionsEnum = UserRoleDTO.PermissionsEnum;

@Component({
  selector: 'jems-call-page',
  templateUrl: './call-page.component.html',
  styleUrls: ['./call-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CallPageComponent implements OnInit {
  PermissionsEnum = PermissionsEnum;
  success = this.router.getCurrentNavigation()?.extras?.state?.success;

  constructor(public callStore: CallStore,
              private callService: CallService,
              private router: Router,
              private changeDetectorRef: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    if (this.success) {
      setTimeout(() => {
        this.success = null;
        this.changeDetectorRef.markForCheck();
      },         3000);
    }
  }

  setCallType(callType: CallDetailDTO.TypeEnum) {
    this.callStore.callType$.next(callType);
  }
}
