
-- Database upgrade from GPL1.0 to Sanofi-RC1

-- fmapp

-- Sequence
--------------------------------------------------------
--  DDL for Sequence SEQ_FM_ID
--------------------------------------------------------

   CREATE SEQUENCE  "FMAPP"."SEQ_FM_ID"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 743 CACHE 20 NOORDER  NOCYCLE ;

-- Tables
--------------------------------------------------------
--  DDL for Table FM_DATA_UID
--------------------------------------------------------

  CREATE TABLE "FMAPP"."FM_DATA_UID" 
   (	"FM_DATA_ID" NUMBER(18,0), 
	"UNIQUE_ID" NVARCHAR2(300), 
	"FM_DATA_TYPE" NVARCHAR2(100)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 0 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS NOLOGGING
  STORAGE(INITIAL 2097152 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index FM_DATA_UID_UK
--------------------------------------------------------

  CREATE UNIQUE INDEX "FMAPP"."FM_DATA_UID_UK" ON "FMAPP"."FM_DATA_UID" ("UNIQUE_ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 NOLOGGING COMPUTE STATISTICS 
  STORAGE(INITIAL 2097152 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "INDX" ;
--------------------------------------------------------
--  DDL for Index FM_DATA_UID_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "FMAPP"."FM_DATA_UID_PK" ON "FMAPP"."FM_DATA_UID" ("FM_DATA_ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 NOLOGGING COMPUTE STATISTICS 
  STORAGE(INITIAL 589824 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "INDX" ;
--------------------------------------------------------
--  Constraints for Table FM_DATA_UID
--------------------------------------------------------

  ALTER TABLE "FMAPP"."FM_DATA_UID" ADD CONSTRAINT "FM_DATA_UID_PK" PRIMARY KEY ("FM_DATA_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 NOLOGGING COMPUTE STATISTICS 
  STORAGE(INITIAL 589824 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "INDX"  ENABLE;
 
  ALTER TABLE "FMAPP"."FM_DATA_UID" ADD CONSTRAINT "FM_DATA_UID_UK" UNIQUE ("UNIQUE_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 NOLOGGING COMPUTE STATISTICS 
  STORAGE(INITIAL 2097152 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "INDX"  ENABLE;
 
  ALTER TABLE "FMAPP"."FM_DATA_UID" MODIFY ("FM_DATA_ID" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_DATA_UID" MODIFY ("UNIQUE_ID" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_DATA_UID" MODIFY ("FM_DATA_TYPE" NOT NULL ENABLE);

--------------------------------------------------------
--  DDL for Table FM_FILE
--------------------------------------------------------

  CREATE TABLE "FMAPP"."FM_FILE" 
   (	"FILE_ID" NUMBER(18,0), 
	"DISPLAY_NAME" NVARCHAR2(1000), 
	"ORIGINAL_NAME" NVARCHAR2(1000), 
	"FILE_VERSION" NUMBER(18,0), 
	"FILE_TYPE" NVARCHAR2(100), 
	"FILE_SIZE" NUMBER(18,0), 
	"FILESTORE_LOCATION" NVARCHAR2(1000), 
	"FILESTORE_NAME" NVARCHAR2(1000), 
	"LINK_URL" NVARCHAR2(1000), 
	"ACTIVE_IND" CHAR(1 BYTE), 
	"CREATE_DATE" DATE, 
	"UPDATE_DATE" DATE, 
	"DESCRIPTION" NVARCHAR2(2000)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index SYS_C0013129
--------------------------------------------------------

  CREATE UNIQUE INDEX "FMAPP"."SYS_C0013129" ON "FMAPP"."FM_FILE" ("FILE_ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table FM_FILE
--------------------------------------------------------

  ALTER TABLE "FMAPP"."FM_FILE" MODIFY ("FILE_ID" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FILE" MODIFY ("DISPLAY_NAME" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FILE" MODIFY ("ORIGINAL_NAME" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FILE" MODIFY ("ACTIVE_IND" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FILE" MODIFY ("CREATE_DATE" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FILE" MODIFY ("UPDATE_DATE" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FILE" ADD PRIMARY KEY ("FILE_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;

--------------------------------------------------------
--  DDL for Trigger TRG_FM_FILE_ID
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FILE_ID" before insert on "FM_FILE"    
for each row begin    
if inserting then      
  if :NEW."FILE_ID" is null then          
    select SEQ_FM_ID.nextval into :NEW."FILE_ID" from dual;       
  end if;    
end if; 
end;
/
ALTER TRIGGER "FMAPP"."TRG_FM_FILE_ID" ENABLE;
--------------------------------------------------------
--  DDL for Trigger TRG_FM_FILE_UID
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FILE_UID" after insert on "FM_FILE"    
for each row
DECLARE
  rec_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO rec_count 
  FROM fm_data_uid 
  WHERE fm_data_id = :new.FILE_ID;
  
  if rec_count = 0 then
    insert into fmapp.fm_data_uid (fm_data_id, unique_id, fm_data_type)
    values (:NEW."FILE_ID", FM_FILE_UID(:NEW."FILE_ID"), 'FM_FILE');
  end if;
end;
/
ALTER TRIGGER "FMAPP"."TRG_FM_FILE_UID" ENABLE;

--------------------------------------------------------
--  DDL for Table FM_FOLDER
--------------------------------------------------------

  CREATE TABLE "FMAPP"."FM_FOLDER" 
   (	"FOLDER_ID" NUMBER(18,0), 
	"FOLDER_NAME" NVARCHAR2(500), 
	"FOLDER_FULL_NAME" NVARCHAR2(500), 
	"FOLDER_LEVEL" NUMBER, 
	"FOLDER_TYPE" NVARCHAR2(50), 
	"FOLDER_TAG" NVARCHAR2(50), 
	"ACTIVE_IND" CHAR(1 BYTE), 
	"OLD_FOLDER_ID" NUMBER(18,0), 
	"PARENT_ID" NUMBER(18,0), 
	"DESCRIPTION" NVARCHAR2(2000)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index SYS_C0011773
--------------------------------------------------------

  CREATE UNIQUE INDEX "FMAPP"."SYS_C0011773" ON "FMAPP"."FM_FOLDER" ("FOLDER_ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table FM_FOLDER
--------------------------------------------------------

  ALTER TABLE "FMAPP"."FM_FOLDER" MODIFY ("FOLDER_NAME" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FOLDER" MODIFY ("FOLDER_FULL_NAME" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FOLDER" MODIFY ("FOLDER_LEVEL" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FOLDER" MODIFY ("FOLDER_TYPE" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FOLDER" ADD PRIMARY KEY ("FOLDER_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;
 
  ALTER TABLE "FMAPP"."FM_FOLDER" MODIFY ("ACTIVE_IND" NOT NULL DISABLE);

--------------------------------------------------------
--  DDL for Trigger TRG_FM_FOLDER_UID
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FOLDER_UID" after insert on "FM_FOLDER"    
for each row
DECLARE
  rec_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO rec_count 
  FROM fm_data_uid 
  WHERE fm_data_id = :new.FOLDER_ID;
  
  if rec_count = 0 then
    insert into fmapp.fm_data_uid (fm_data_id, unique_id, fm_data_type)
    values (:NEW."FOLDER_ID", FM_FOLDER_UID(:NEW."FOLDER_ID"), 'FM_FOLDER');
  end if;
end;
/
ALTER TRIGGER "FMAPP"."TRG_FM_FOLDER_UID" ENABLE;
--------------------------------------------------------
--  DDL for Trigger TRG_FM_FOLDER_ID
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FOLDER_ID" before insert on FM_FOLDER    
for each row begin    
if inserting then      
  if :NEW.FOLDER_ID is null then          
    select SEQ_FM_ID.nextval into :NEW.FOLDER_ID from dual;       
  end if;
  if :new.FOLDER_FULL_NAME is null then
    if :new.PARENT_ID is null then
      select '\' || fm_folder_uid(:new.folder_id) || '\' into :new.folder_full_name 
      from dual;
    else
      select folder_full_name || fm_folder_uid(:new.folder_id) || '\' into :new.folder_full_name 
      from fm_folder
      where folder_id = :new.parent_id;
    end if;
  end if;
end if; 
end;
/
ALTER TRIGGER "FMAPP"."TRG_FM_FOLDER_ID" ENABLE;

--------------------------------------------------------
--  DDL for Table FM_FOLDER_ASSOCIATION
--------------------------------------------------------

  CREATE TABLE "FMAPP"."FM_FOLDER_ASSOCIATION" 
   (	"FOLDER_ID" NUMBER(18,0), 
	"OBJECT_UID" NVARCHAR2(300), 
	"OBJECT_TYPE" NVARCHAR2(100)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table FM_FOLDER_ASSOCIATION
--------------------------------------------------------

  ALTER TABLE "FMAPP"."FM_FOLDER_ASSOCIATION" ADD CONSTRAINT "PK_FOLDER_ASSOC" PRIMARY KEY ("FOLDER_ID", "OBJECT_UID") DISABLE;
 
  ALTER TABLE "FMAPP"."FM_FOLDER_ASSOCIATION" MODIFY ("FOLDER_ID" NOT NULL DISABLE);
 
  ALTER TABLE "FMAPP"."FM_FOLDER_ASSOCIATION" MODIFY ("OBJECT_UID" NOT NULL DISABLE);
 
  ALTER TABLE "FMAPP"."FM_FOLDER_ASSOCIATION" MODIFY ("OBJECT_TYPE" NOT NULL DISABLE);
--------------------------------------------------------
--  Ref Constraints for Table FM_FOLDER_ASSOCIATION
--------------------------------------------------------

  ALTER TABLE "FMAPP"."FM_FOLDER_ASSOCIATION" ADD CONSTRAINT "FK_FM_FOLDER_ASSOC_FM_FOLDER" FOREIGN KEY ("FOLDER_ID")
	  REFERENCES "FMAPP"."FM_FOLDER" ("FOLDER_ID") ENABLE;

--------------------------------------------------------
--  DDL for Table FM_FOLDER_FILE_ASSOCIATION
--------------------------------------------------------

  CREATE TABLE "FMAPP"."FM_FOLDER_FILE_ASSOCIATION" 
   (	"FOLDER_ID" NUMBER(18,0), 
	"FILE_ID" NUMBER(18,0)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index PK_FOLDER_FILE_ASSOC
--------------------------------------------------------

  CREATE UNIQUE INDEX "FMAPP"."PK_FOLDER_FILE_ASSOC" ON "FMAPP"."FM_FOLDER_FILE_ASSOCIATION" ("FOLDER_ID", "FILE_ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table FM_FOLDER_FILE_ASSOCIATION
--------------------------------------------------------

  ALTER TABLE "FMAPP"."FM_FOLDER_FILE_ASSOCIATION" ADD CONSTRAINT "PK_FOLDER_FILE_ASSOC" PRIMARY KEY ("FOLDER_ID", "FILE_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;
 
  ALTER TABLE "FMAPP"."FM_FOLDER_FILE_ASSOCIATION" MODIFY ("FOLDER_ID" NOT NULL ENABLE);
 
  ALTER TABLE "FMAPP"."FM_FOLDER_FILE_ASSOCIATION" MODIFY ("FILE_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table FM_FOLDER_FILE_ASSOCIATION
--------------------------------------------------------

  ALTER TABLE "FMAPP"."FM_FOLDER_FILE_ASSOCIATION" ADD CONSTRAINT "FK_FM_FOLDER_FM_FOLDER" FOREIGN KEY ("FOLDER_ID")
	  REFERENCES "FMAPP"."FM_FOLDER" ("FOLDER_ID") ENABLE;
 
  ALTER TABLE "FMAPP"."FM_FOLDER_FILE_ASSOCIATION" ADD CONSTRAINT "FK_FOLDER_FILE_ASSOC_FILE" FOREIGN KEY ("FILE_ID")
	  REFERENCES "FMAPP"."FM_FILE" ("FILE_ID") ENABLE;

--------------------------------------------------------
--  DDL for Function FM_FILE_UID
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "FMAPP"."FM_FILE_UID" (
  FILE_ID NUMBER
) RETURN VARCHAR2 AS
BEGIN
  -- $Id$
  -- Creates uid for bio_concept_code.

  RETURN 'FIL:' || FILE_ID;
END FM_FILE_UID;

/

--------------------------------------------------------
--  DDL for Function FM_FOLDER_UID
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "FMAPP"."FM_FOLDER_UID" (
  FOLDER_ID NUMBER
) RETURN VARCHAR2 AS
BEGIN
  -- $Id$
  -- Creates uid for bio_concept_code.

  RETURN 'FOL:' || FOLDER_ID;
END FM_FOLDER_UID;

/

--------------------------------------------------------
--  DDL for Function FM_GET_FOLDER_FULL_NAME
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "FMAPP"."FM_GET_FOLDER_FULL_NAME" (
  p_folder_id number
)
return nvarchar2
as
  v_parent_id number;
  v_folder_full_name nvarchar2(1000);
begin

  select parent_id into v_parent_id
  from fm_folder
  where folder_id = p_folder_id;
  
  v_folder_full_name := fm_folder_uid(p_folder_id) || '\';
  
  while v_parent_id is not null
  loop
    v_folder_full_name := fm_folder_uid(v_parent_id) || '\' || v_folder_full_name;

    select parent_id into v_parent_id
    from fm_folder
    where folder_id = v_parent_id;
  end loop;

  v_folder_full_name := '\' || v_folder_full_name;
  
  return v_folder_full_name;  
end;

/

--------------------------------------------------------
--  DDL for Function ID_TO_TAG
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "FMAPP"."ID_TO_TAG" (p_id number)
return varchar
as
  v_id number := p_id;
  v_mod number;
  v_tag_char char;
  v_digits constant varchar2(36) := '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  v_tag varchar2(50) := '';
  
begin

  if p_id = 0 then
    v_tag := '0';
  end if;
  
  while v_id != 0 loop
    v_mod := v_id mod 36;
    v_tag_char := substr(v_digits, v_mod + 1, 1);
    v_tag := v_tag_char || v_tag;
    v_id := floor(v_id / 36);
  end loop;
  
  return v_tag;

end;

/

--------------------------------------------------------
--  DDL for Procedure FM_UPDATE_FOLDER_FULL_NAME
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "FMAPP"."FM_UPDATE_FOLDER_FULL_NAME" 
as
  v_folder_full_name nvarchar2(1000);
  cursor folder_ids is
    select folder_id
    from fm_folder;
    
begin
  for folder_rec in folder_ids
  loop
    select fm_get_folder_full_name(folder_rec.folder_id) into v_folder_full_name
    from dual;
    
    update fm_folder set folder_full_name = v_folder_full_name
    where folder_id = folder_rec.folder_id;
  end loop;
end;

/

--------------------------------------------------------
--  DDL for Trigger TRG_FM_FILE_ID
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FILE_ID" before insert on "FM_FILE"    
for each row begin    
if inserting then      
  if :NEW."FILE_ID" is null then          
    select SEQ_FM_ID.nextval into :NEW."FILE_ID" from dual;       
  end if;    
end if; 
end;
/
ALTER TRIGGER "FMAPP"."TRG_FM_FILE_ID" ENABLE;

--------------------------------------------------------
--  DDL for Trigger TRG_FM_FOLDER_ID
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FOLDER_ID" before insert on FM_FOLDER    
for each row begin    
if inserting then      
  if :NEW.FOLDER_ID is null then          
    select SEQ_FM_ID.nextval into :NEW.FOLDER_ID from dual;       
  end if;
  if :new.FOLDER_FULL_NAME is null then
    if :new.PARENT_ID is null then
      select '\' || fm_folder_uid(:new.folder_id) || '\' into :new.folder_full_name 
      from dual;
    else
      select folder_full_name || fm_folder_uid(:new.folder_id) || '\' into :new.folder_full_name 
      from fm_folder
      where folder_id = :new.parent_id;
    end if;
  end if;
end if; 
end;
/
ALTER TRIGGER "FMAPP"."TRG_FM_FOLDER_ID" ENABLE;

--------------------------------------------------------
--  DDL for Trigger TRG_FM_FOLDER_UID
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FOLDER_UID" after insert on "FM_FOLDER"    
for each row
DECLARE
  rec_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO rec_count 
  FROM fm_data_uid 
  WHERE fm_data_id = :new.FOLDER_ID;
  
  if rec_count = 0 then
    insert into fmapp.fm_data_uid (fm_data_id, unique_id, fm_data_type)
    values (:NEW."FOLDER_ID", FM_FOLDER_UID(:NEW."FOLDER_ID"), 'FM_FOLDER');
  end if;
end;
/
ALTER TRIGGER "FMAPP"."TRG_FM_FOLDER_UID" ENABLE;

