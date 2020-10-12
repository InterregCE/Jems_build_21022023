import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {TableComponent} from './components/table/table.component';
import {MatTableModule} from '@angular/material/table';
import {NgxPermissionsModule} from 'ngx-permissions';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {PaginatorComponent} from '@common/components/table/paginator/paginator.component';
import {MatTabsModule} from '@angular/material/tabs';
import {MatDialogModule} from '@angular/material/dialog';
import {RouterModule} from '@angular/router';
import {ProjectApplicationListComponent} from '../project/project-application/components/project-application-list/project-application-list.component';

const modules = [
  CommonModule,
  TranslateModule,
  FormsModule,
  ReactiveFormsModule,
  MatButtonModule,
  MatInputModule,
  MatTableModule,
  MatDialogModule,
  MatPaginatorModule,
  MatSortModule,
  MatTabsModule,
  NgxPermissionsModule.forChild(),
  RouterModule,
];

const declarations = [
  ProjectApplicationListComponent,
  TableComponent,
  PaginatorComponent,
];

@NgModule({
  declarations: [
    declarations
  ],
  imports: [
    modules,
  ],
  exports: [
    modules,
    declarations
  ]
})
export class SharedModule {
}