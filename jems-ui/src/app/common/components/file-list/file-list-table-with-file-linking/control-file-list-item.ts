import {ProjectReportFileDTO, ProjectReportFileMetadataDTO, UserSimpleDTO} from '@cat/api';

export interface ControlFileListItem {
  id: number;
  name: string;
  type: ProjectReportFileDTO.TypeEnum;
  uploaded: Date;
  author: UserSimpleDTO;
  sizeString: string;
  description: string;
  editable: boolean;
  deletable: boolean;
  tooltipIfNotDeletable: string;
  iconIfNotDeletable: string;
  parentEntityId?: number;
  attachment?: ProjectReportFileMetadataDTO;
}
