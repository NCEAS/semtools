
DELETE FROM context_instance;
DELETE FROM measurement_instance;
DELETE FROM observation_instance;
DELETE FROM entity_instance;
DELETE FROM data_annotation;
ALTER SEQUENCE did_seq RESTART WITH 1;

DELETE FROM key_attr;
DELETE FROM map;
DELETE FROM context_type;
DELETE FROM measurement_type;
DELETE FROM observation_type;
DELETE FROM annotation;
ALTER SEQUENCE annot_id_seq RESTART WITH 1;

DROP TABLE context_instance CASCADE;
DROP TABLE measurement_instance CASCADE;
DROP TABLE observation_instance CASCADE;
DROP TABLE entity_instance CASCADE;
DROP TABLE data_annotation CASCADE;
DROP SEQUENCE did_seq;

DROP TABLE key_attr;
DROP TABLE map CASCADE;
DROP TABLE context_type CASCADE;
DROP TABLE measurement_type CASCADE;
DROP TABLE observation_type CASCADE;
DROP TABLE annotation CASCADE;
DROP SEQUENCE annot_id_seq;

CREATE SEQUENCE annot_id_seq;
CREATE TABLE annotation(
	annot_id bigint DEFAULT NEXTVAL('annot_id_seq') PRIMARY KEY, 
	annot_uri varchar(256));
	
CREATE TABLE observation_type(
	annot_id bigint REFERENCES annotation(annot_id),
	otypelabel char(16), 
	ename char(32), 
	is_distinct boolean,
	PRIMARY KEY (annot_id,otypelabel));
	
CREATE TABLE measurement_type(
	annot_id bigint REFERENCES annotation(annot_id),
	mtypelabel char(16), 
	otypelabel char(16),
	iskey boolean,
	characteristic char(64), 
	standard char(16), 
	protocal varchar(256),
	PRIMARY KEY (annot_id,mtypelabel));
	
CREATE TABLE context_type(
	annot_id bigint REFERENCES annotation(annot_id),
	otypelabel char(16),
	context_otypelabel char(16),
	relationship_name char(8),
	is_identifying boolean);
CREATE TABLE map(
	annot_id bigint REFERENCES annotation(annot_id),
	mtypelabel char(16),
	attrname char(16),
	mapcond varchar(64),
	mapval varchar(32));
CREATE TABLE key_attr(
	annot_id bigint, 
	ename char(32),
	keyattr varchar(256)
	);	
CREATE SEQUENCE did_seq;
CREATE TABLE data_annotation(
	did bigint DEFAULT NEXTVAL('did_seq') PRIMARY KEY, 
	annot_id bigint REFERENCES annotation (annot_id),
	dataset_file varchar(256));
CREATE TABLE entity_instance(
	eid bigint PRIMARY KEY,
	did bigint REFERENCES data_annotation(did), 
	record_id bigint, 
	etype varchar(64));		
CREATE TABLE observation_instance(
	oid bigint PRIMARY KEY,
	did bigint REFERENCES data_annotation(did), 
	record_id bigint, 
	eid bigint REFERENCES entity_instance(eid),	 
	otypelabel char(32)
	);
CREATE TABLE measurement_instance(
	mid bigint PRIMARY KEY,
	did bigint REFERENCES data_annotation(did),
	record_id bigint, 
	oid bigint REFERENCES observation_instance (oid),
	mtypelabel char(32), 
	mvalue varchar(64));
CREATE TABLE context_instance(	
	did bigint REFERENCES data_annotation(did), 
	record_id bigint, 
	oid bigint REFERENCES observation_instance (oid),
	context_oid bigint REFERENCES observation_instance (oid), 
	rtype varchar(64));
	