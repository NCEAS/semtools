
create table mi_numeric AS 
(SELECT mid,did,record_id,oid,mtypelabel,CAST (mvalue AS numeric) 
FROM measurement_instance 
WHERE mvalue ~ '^[-]?[0-9]+' AND mvalue !~ '[a-zA-Z]+');

create index mi_numeric_mvalue_idx on mi_numeric(mvalue);
create index mi_numeric_mtypelabel_idx on mi_numeric(mtypelabel);

create table mi_string AS 
(SELECT mid,did,record_id,oid,mtypelabel,mvalue FROM measurement_instance WHERE  mvalue ~ '[a-zA-Z]+');