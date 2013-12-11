CREATE OR REPLACE VIEW "BIOMART"."VW_FACETED_SEARCH_FILE" AS
select f.folder_id as FOLDER_ID, fa.object_uid as OBJECT_UID, bio_data_id as STUDY_ID, x.filenames as FILE_TEXT
from fmapp.fm_folder f
inner join fmapp.fm_folder_association fa on fa.folder_id = f.folder_id
inner join biomart.bio_data_uid buid ON fa.object_uid = buid.unique_id
inner join biomart.bio_experiment be ON buid.bio_data_id = be.bio_experiment_id
left outer join
  (select id, filenames from
    (select
      ff.folder_id as id, fi.display_name || ' ' || fi.description as filename, 'FILE' as row_type
      FROM fmapp.fm_folder ff
      JOIN fmapp.fm_folder_file_association ffa on ff.folder_id = ffa.folder_id
      JOIN fmapp.fm_file fi on fi.file_id = ffa.file_id
    )
    pivot (
      listagg(to_char(filename), ' ') within group (order by filename)
      for row_type in ('FILE' as filenames)
    )
  ) x on x.id = f.folder_id
;