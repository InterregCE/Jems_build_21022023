import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {TableConfiguration} from '@common/components/table/model/table.configuration';
import {OutputWorkPackageSimple} from '@cat/api';
import {ActivatedRoute} from '@angular/router';
import {ColumnType} from '@common/components/table/model/column-type.enum';
import {Forms} from '../../../../../common/utils/forms';
import {filter, map, take} from 'rxjs/operators';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-project-application-form-work-packages-list',
  templateUrl: './project-application-form-work-packages-list.component.html',
  styleUrls: ['./project-application-form-work-packages-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFormWorkPackagesListComponent implements OnInit {
  projectId = this.activatedRoute.snapshot.params.projectId;

  @Input()
  workPackages: OutputWorkPackageSimple[];
  @Input()
  editable: boolean;
  @Output()
  deleteWorkPackage = new EventEmitter<number>();

  @ViewChild('deletionCell', {static: true})
  deletionCell: TemplateRef<any>;
  @ViewChild('titleCell', {static: true})
  titleCell: TemplateRef<any>;

  tableConfiguration: TableConfiguration;

  constructor(private activatedRoute: ActivatedRoute,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.tableConfiguration = new TableConfiguration({
      routerLink: `/app/project/detail/${this.projectId}/applicationFormWorkPackage/detail`,
      isTableClickable: true,
      sortable: false,
      columns: [
        {
          displayedColumn: 'project.application.form.workpackage.number',
          elementProperty: 'number',
          alternativeValueCondition: (element: any) => {
            return element === null;
          },
          alternativeValue: 'project.application.form.partner.number.info.auto',
          sortProperty: 'number'
        },
        {
          displayedColumn: 'project.application.form.workpackage.name',
          columnType: ColumnType.CustomComponent,
          customCellTemplate: this.titleCell
        },
        {
          displayedColumn: ' ',
          columnType: ColumnType.CustomComponent,
          customCellTemplate: this.deletionCell
        },
      ]
    });
  }

  delete(workPackage: OutputWorkPackageSimple, name: string): void {
    const i18nKey = name
      ? 'project.application.form.workpackage.table.action.delete.dialog.message'
      : 'project.application.form.workpackage.table.action.delete.dialog.message.no.name';

    Forms.confirm(
      this.dialog,
      {
        title: 'project.application.form.workpackage.table.action.delete.dialog.header',
        message: {i18nKey, i18nArguments: {name}},
        warnMessage: 'project.application.form.workpackage.table.action.delete.dialog.warning'
      }).pipe(
      take(1),
      filter(answer => !!answer),
      map(() => this.deleteWorkPackage.emit(workPackage.id)),
    ).subscribe();
  }
}
