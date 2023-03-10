import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Alert} from '@common/components/forms/alert';
import {ConfirmDialogData} from '@common/components/modals/confirm-dialog/confirm-dialog.data';

@Component({
  selector: 'jems-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.scss']
})
export class ConfirmDialogComponent {
  Alert = Alert;

  data: ConfirmDialogData;

  constructor(public dialogRef: MatDialogRef<ConfirmDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public confirmDialogData: ConfirmDialogData) {
    this.data = confirmDialogData;
  }

  get message(): string {
    return (this.data?.message as any)?.i18nKey || this.data?.message;
  }

  get arguments(): any {
    return (this.data?.message as any)?.i18nArguments;
  }

  get warnMessage(): string {
    return (this.data?.warnMessage as any)?.i18nKey || this.data?.warnMessage;
  }

  get warnArguments(): any {
    return (this.data?.warnMessage as any)?.i18nArguments;
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onDismiss(): void {
    this.dialogRef.close(false);
  }
}

