
#data
#CREATE TABLE data_annotation(did bigint, annot_id bigint, dataset_file varchar(256)); 

DROP TABLE context_relationship;
DROP TABLE measurement_type;
DROP TABLE observation_type;
DROP TABLE annotation;
DROP SEQUENCE annot_id_seq;

DROP TABLE entity_instance;
DROP TABLE observation_instance;
DROP TABLE measurement_instance;
DROP TABLE context_instance;

CREATE TABLE entity_instance(record_id varchar(256), eid bigint, etype varchar(64));
CREATE TABLE observation_instance(record_id varchar(256), eid bigint, oid bigint, otype varchar(64));
CREATE TABLE measurement_instance(record_id varchar(256), oid bigint, mid bigint, mlabel varchar(64), mvalue varchar(64));
CREATE TABLE context_instance(record_id varchar(256), context_record_id varchar(256), oid bigint, context_oid bigint, rtype varchar(64));

CREATE SEQUENCE annot_id_seq;
CREATE TABLE annotation(
	annot_id bigint DEFAULT NEXTVAL('annot_id_seq') PRIMARY KEY, 
	annot_uri varchar(256));

CREATE TABLE observation_type(
	annot_id bigint REFERENCES annotation(annot_id),
	olabel varchar(64), 
	ename varchar(64), 
	is_distinct boolean);
	
CREATE TABLE measurement_type(
	annot_id bigint REFERENCES annotation(annot_id),
	mtype char(16), 
	olabel varchar(64),
	iskey boolean,
	characteristic char(64), 
	standard char(16), 
	protocal varchar(256));
	
CREATE TABLE context_relationship(
	annot_id bigint REFERENCES annotation(annot_id),
	olabel varchar(64),
	context_olabel varchar(64),
	relationshipname char(16),
	is_identifying boolean);
	
	
