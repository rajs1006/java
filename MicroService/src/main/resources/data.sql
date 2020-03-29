-- This table is bound with ServiceEnum.java and so name should be same
-- while inserting data.
insert into services(service, sequence) VALUES
                              ('LoadArticleService', 1),
                              ('PendingArticleService', 2),
                              ('FailedArticleService', 3);

-- Publication, in java we read this value in lower_case
insert into publications(publication) VALUES
--                                       ('hao'),
                                      ('bmo');

-- publication_id is from publications table
insert into properties(publication_id, sitemap_file)
  SELECT atg.id, C2
    FROM (
          VALUES
--           ('hao', 'https://uat.abendblatt.de/sitemaps/archive.xml'),
          ('bmo', 'https://uat.morgenpost.de/sitemaps/archive.xml')
    )
  join publications AS atg on (atg.publication = C1);

-- delay is service trigger interval in seconds
-- publication_id is from publications table
-- service_id is from services table.
insert into schedules(publication_id, service_id, enabled, delay)
  SELECT atg.id, ser.id, C3, C4
    FROM (
          VALUES
--           ('hao', 'LoadArticleService', true, 604800),
--           ('hao', 'PendingArticleService', true, 120),
--           ('hao', 'FailedArticleService', true, 180),
          ('bmo', 'LoadArticleService', true, 604800),
          ('bmo', 'PendingArticleService', true, 120),
          ('bmo', 'FailedArticleService', true, 180)
    )
  join publications AS atg on (atg.publication = C1)
  join services AS ser on (ser.service = C2);