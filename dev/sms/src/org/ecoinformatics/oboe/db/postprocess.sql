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

select count(*) from measurement_instance;
select count(*) from mi_numeric;
select count(*) from mi_string;
# query 1 result = (query 2 result) + (query 3 result)

==============================================================================
CREATE VIEW non_agg_meas_view AS 
(SELECT oi.did,oic.compressed_record_id as record_id, oi.oid, oi.etype, mi.mvalue, mt.characteristic,mt.standard
FROM mi_numeric AS mi,observation_instance AS oi,measurement_type AS mt,oi_compress AS oic 
WHERE oi.oid=mi.oid AND mt.mtypelabel = mi.mtypelabel AND mt.annot_id = mi.did AND oi.oid=oic.oid AND oi.did=oic.did);

#View def1: NO USE
CREATE VIEW non_agg_meas_view_basic AS 
(SELECT DISTINCT oi.did,oi.record_id, oi.oid, oi.etype, mi.mvalue, mt.characteristic,mt.standard
FROM mi_numeric AS mi,observation_instance AS oi,measurement_type AS mt
WHERE oi.oid=mi.oid AND mt.mtypelabel = mi.mtypelabel AND mt.annot_id = mi.did);

#View def2: 
CREATE VIEW non_agg_meas_view_basic AS 
(SELECT oi.did,oi.record_id, oi.oid, oi.etype, mi.mvalue, mt.characteristic,mt.standard
FROM mi_numeric AS mi,observation_instance AS oi,measurement_type AS mt
WHERE oi.oid=mi.oid AND mt.mtypelabel = mi.mtypelabel AND mt.annot_id = mi.did);

CREATE INDEX observation_instance_etype_idx on observation_instance(etype);

#View def3: same to def2
CREATE VIEW non_agg_meas_view_onlydid AS 
(SELECT oi.did,oi.etype, mi.mvalue, mt.characteristic
FROM mi_numeric AS mi,observation_instance AS oi,measurement_type AS mt
WHERE oi.oid=mi.oid AND mt.mtypelabel = mi.mtypelabel AND mt.annot_id = mi.did);


CREATE TABLE omi_numeric as (
SELECT mi.mid, mi.did, mi.record_id,mi.oid,mi.mtypelabel,mi.mvalue,oi.etype,oi.otypelabel 
from mi_numeric as mi, observation_instance as oi 
WHERE oi.oid = mi.oid);
CREATE INDEX omi_numeric_mvalue_idx on omi_numeric(mvalue);
CREATE INDEX omi_numeric_etype_idx on omi_numeric(etype);


DROP TABLE omi_numeric_full;
CREATE TABLE omi_numeric_full as (
SELECT mi.mid, mi.did, mi.record_id,mi.oid,mi.mtypelabel,mi.mvalue,oi.etype,oi.otypelabel,mt.characteristic,mt.standard
FROM mi_numeric as mi, observation_instance as oi, measurement_type AS mt
WHERE oi.oid = mi.oid AND mt.mtypelabel = mi.mtypelabel and mt.annot_id = mi.did
);
CREATE INDEX omi_numeric_full_mvalue_idx on omi_numeric_full(mvalue);
CREATE INDEX omi_numeric_full_etype_idx on omi_numeric_full(etype);
CREATE INDEX omi_numeric_full_characteristic_idx on omi_numeric_full(characteristic);


CREATE VIEW mtb_view AS 
(SELECT omi.did,omi.record_id, omi.oid, omi.etype, omi.mvalue, mt.characteristic,mt.standard
FROM omi_numeric AS omi,measurement_type AS mt
WHERE mt.mtypelabel = omi.mtypelabel AND mt.annot_id = omi.did);

		