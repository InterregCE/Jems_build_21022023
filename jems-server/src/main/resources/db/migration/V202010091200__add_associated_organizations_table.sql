CREATE TABLE project_associated_organization
(
    id                               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id                       INT UNSIGNED NOT NULL,
    partner_id                       INT UNSIGNED NOT NULL,
    name_in_original_language        VARCHAR(127) NOT NULL,
    name_in_english                  VARCHAR(127) NOT NULL,
    sort_number                      INT DEFAULT NULL,
    CONSTRAINT fk_project_associated_organization_project
        FOREIGN KEY (project_id) REFERENCES project (id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,
    CONSTRAINT fk_project_associated_organization_partner
        FOREIGN KEY (partner_id) REFERENCES project_partner (id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT
);

CREATE TABLE associated_organization_contact
(
    partner_id INT UNSIGNED                                  NOT NULL,
    type       ENUM ('LegalRepresentative', 'ContactPerson') NOT NULL,
    title      VARCHAR(25)  DEFAULT NULL,
    first_name VARCHAR(50)  DEFAULT NULL,
    last_name  VARCHAR(50)  DEFAULT NULL,
    email      VARCHAR(255) DEFAULT NULL,
    telephone  VARCHAR(25)  DEFAULT NULL,
    PRIMARY KEY (partner_id, type),
    CONSTRAINT fk_associated_organization_contact_associated_organization FOREIGN KEY (partner_id) REFERENCES project_associated_organization (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);

CREATE TABLE project_associated_organization_details
(
    organization_id INT UNSIGNED PRIMARY KEY ,
    country         VARCHAR(100) DEFAULT NULL,
    nuts_region2    VARCHAR(100) DEFAULT NULL,
    nuts_region3    VARCHAR(100) DEFAULT NULL,
    street          VARCHAR(50)  DEFAULT NULL,
    house_number    VARCHAR(20)  DEFAULT NULL,
    postal_code     VARCHAR(20)  DEFAULT NULL,
    city            VARCHAR(50)  DEFAULT NULL,
    homepage        VARCHAR(250) DEFAULT NULL,
    CONSTRAINT fk_partner_organization_details_to_associated_organization FOREIGN KEY (organization_id) REFERENCES project_associated_organization (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);
