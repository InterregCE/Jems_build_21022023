SELECT id INTO @id FROM account_role WHERE `name` = 'administrator' ORDER BY id DESC LIMIT 1;
INSERT INTO account_role_permission(account_role_id, permission)
VALUES (@id, 'ProjectModificationView'),
       (@id, 'ProjectStatusDecideModificationApproved'),
       (@id, 'ProjectStatusDecideModificationNotApproved'),
       (@id, 'ProjectOpenModification'),
       (@id, 'ProjectModificationFileAssessmentRetrieve'),
       (@id, 'ProjectModificationFileAssessmentUpdate');

SELECT id INTO @id FROM account_role WHERE `name` = 'programme user' ORDER BY id DESC LIMIT 1;
INSERT INTO account_role_permission(account_role_id, permission)
VALUES (@id, 'ProjectModificationView'),
       (@id, 'ProjectStatusDecideModificationApproved'),
       (@id, 'ProjectStatusDecideModificationNotApproved'),
       (@id, 'ProjectOpenModification'),
       (@id, 'ProjectModificationFileAssessmentRetrieve'),
       (@id, 'ProjectModificationFileAssessmentUpdate');

