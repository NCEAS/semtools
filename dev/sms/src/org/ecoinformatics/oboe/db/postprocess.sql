drop view non_agg_meas_view;
drop table mi_string;
drop table mi_numeric;


create table mi_numeric AS 
(SELECT mid,did,record_id,oid,mtypelabel,CAST (mvalue AS numeric) 
FROM measurement_instance 
WHERE mvalue ~ '^[-]?[0-9]+' AND mvalue !~ '[a-zA-Z]+');

create index mi_numeric_mvalue_idx on mi_numeric(mvalue);
create index mi_numeric_mtypelabel_idx on mi_numeric(mtypelabel);

create table mi_string AS 
(SELECT mid,did,record_id,oid,mtypelabel,mvalue FROM measurement_instance WHERE  mvalue ~ '[a-zA-Z]+');

CREATE VIEW non_agg_meas_view AS 
(SELECT DISTINCT oi.did,oic.compressed_record_id as record_id, oi.oid, oi.etype, mi.mvalue, mt.characteristic,mt.standard
FROM mi_numeric AS mi,observation_instance AS oi,measurement_type AS mt,oi_compress AS oic 
WHERE oi.oid=mi.oid AND mt.mtypelabel = mi.mtypelabel AND oi.oid=oic.oid AND oi.did=oic.did);

#View def1: the execution takes much more time compared with View def2
CREATE VIEW non_agg_meas_view_basic AS 
(SELECT DISTINCT oi.did,oi.record_id, oi.oid, oi.etype, mi.mvalue, mt.characteristic,mt.standard
FROM mi_numeric AS mi,observation_instance AS oi,measurement_type AS mt
WHERE oi.oid=mi.oid AND mt.mtypelabel = mi.mtypelabel);

#View def2
CREATE VIEW non_agg_meas_view_basic AS 
(SELECT oi.did,oi.record_id, oi.oid, oi.etype, mi.mvalue, mt.characteristic,mt.standard
FROM mi_numeric AS mi,observation_instance AS oi,measurement_type AS mt
WHERE oi.oid=mi.oid AND mt.mtypelabel = mi.mtypelabel);

#View def3
CREATE VIEW non_agg_meas_view_onlydid AS 
(SELECT oi.did,oi.etype, mi.mvalue, mt.characteristic
FROM mi_numeric AS mi,observation_instance AS oi,measurement_type AS mt
WHERE oi.oid=mi.oid AND mt.mtypelabel = mi.mtypelabel);

select count(*) from measurement_instance;
select count(*) from mi_numeric;
select count(*) from mi_string;
# query 1 result = (query 2 result) + (query 3 result)
