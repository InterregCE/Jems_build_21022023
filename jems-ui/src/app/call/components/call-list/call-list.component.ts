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
import {ColumnType} from '@common/components/table/model/column-type.enum';
import {MatSort} from '@angular/material/sort';
import {PageOutputCallList} from '@cat/api'
import {Alert} from '@common/components/forms/alert';
import {Permission} from '../../../security/permissions/permission';
import {Router} from '@angular/router';
import {BaseComponent} from '@common/components/base-component';

@Component({
  selector: 'app-call-list',
  templateUrl: './call-list.component.html',
  styleUrls: ['./call-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CallListComponent extends BaseComponent implements OnInit {
  @ViewChild('callActionsCell', {static: true})
  actionsCell: TemplateRef<any>;

  Alert = Alert;
  Permission = Permission;

  @Input()
  publishedCall: string;
  @Input()
  callPage: PageOutputCallList;
  @Input()
  pageIndex: number;
  @Input()
  isApplicant: boolean;

  @Output()
  newPageSize: EventEmitter<number> = new EventEmitter<number>();
  @Output()
  newPageIndex: EventEmitter<number> = new EventEmitter<number>();
  @Output()
  newSort: EventEmitter<Partial<MatSort>> = new EventEmitter<Partial<MatSort>>();

  tableConfigurationProgramme: TableConfiguration;
  tableConfigurationApplicant: TableConfiguration;

  constructor(private router: Router) {
    super();
  }

  ngOnInit() {
    this.tableConfigurationProgramme = new TableConfiguration({
      routerLink: '/app/call/detail',
      isTableClickable: true,
      columns: [
        {
          displayedColumn: 'call.table.column.name.id',
          elementProperty: 'id',
          sortProperty: 'id'
        },
        {
          displayedColumn: 'call.table.column.name.name',
          elementProperty: 'name',
          sortProperty: 'name',
        },
        {
          displayedColumn: 'call.table.column.name.status',
          elementProperty: 'status',
          elementTranslationKey: 'common.label.callstatus',
          sortProperty: 'status'
        },
        {
          displayedColumn: 'call.table.column.name.started',
          columnType: ColumnType.Date,
          elementProperty: 'startDate',
          sortProperty: 'startDate'
        },
        {
          displayedColumn: 'call.table.column.name.end',
          columnType: ColumnType.Date,
          elementProperty: 'endDate',
          sortProperty: 'endDate'
        }
      ]
    });

    this.tableConfigurationApplicant = new TableConfiguration({
      isTableClickable: false,
      columns: [
        {
          displayedColumn: 'call.table.column.name.id',
          elementProperty: 'id',
          sortProperty: 'id'
        },
        {
          displayedColumn: 'call.table.column.name.name',
          elementProperty: 'name',
          sortProperty: 'name',
        },
        {
          displayedColumn: 'call.table.column.name.status',
          elementProperty: 'status',
          elementTranslationKey: 'common.label.callstatus',
          sortProperty: 'status'
        },
        {
          displayedColumn: 'call.table.column.name.started',
          columnType: ColumnType.Date,
          elementProperty: 'startDate',
          sortProperty: 'startDate'
        },
        {
          displayedColumn: 'call.table.column.name.end',
          columnType: ColumnType.Date,
          elementProperty: 'endDate',
          sortProperty: 'endDate'
        },
        {
          displayedColumn: 'call.table.column.name.action',
          customCellTemplate: this.actionsCell
        }
      ]
    });
  }

  applyToCall(callId: number): void {
    this.router.navigate(['/app/project/applyTo/' + callId]);
  }
}