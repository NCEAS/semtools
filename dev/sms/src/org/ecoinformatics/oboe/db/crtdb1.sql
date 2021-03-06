
DELETE FROM map;
DELETE FROM context_relationship;
DELETE FROM measurement_type;
DELETE FROM observation_type;

DELETE FROM context_instance;
DELETE FROM measurement_instance;
ALTER SEQUENCE mid_seq RESTART WITH 1;
DELETE FROM observation_instance;
ALTER SEQUENCE oid_seq RESTART WITH 1;
DELETE FROM entity_instance;
ALTER SEQUENCE eid_seq RESTART WITH 1;
DELETE FROM data_annotation;
ALTER SEQUENCE did_seq RESTART WITH 1;

DELETE FROM annotation;
ALTER SEQUENCE annot_id_seq RESTART WITH 1;

DROP TABLE map CASCADE;
DROP TABLE context_relationship CASCADE;
DROP TABLE measurement_type CASCADE;
DROP TABLE observation_type CASCADE;

DROP TABLE context_instance CASCADE;
DROP TABLE measurement_instance CASCADE;
DROP SEQUENCE mid_seq;
DROP TABLE observation_instance CASCADE;
DROP SEQUENCE oid_seq;
DROP TABLE entity_instance CASCADE;
DROP SEQUENCE eid_seq;
DROP TABLE data_annotation CASCADE;
DROP SEQUENCE did_seq;

DROP TABLE annotation CASCADE;
DROP SEQUENCE annot_id_seq;

CREATE SEQUENCE annot_id_seq;
CREATE TABLE annotation(
	annot_id bigint DEFAULT NEXTVAL('annot_id_seq') PRIMARY KEY, 
	annot_uri varchar(256));
	
CREATE TABLE observation_type(
	annot_id bigint REFERENCES annotation(annot_id),
	otypelabel char(32), 
	ename varchar(64), 
	is_distinct boolean,
	PRIMARY KEY (annot_id,otypelabel));
	
CREATE TABLE measurement_type(
	annot_id bigint REFERENCES annotation(annot_id),
	mtypelabel char(16), 
	otypelabel char(32),
	iskey boolean,
	characteristic char(64), 
	standard char(16), 
	protocal varchar(256),
	PRIMARY KEY (annot_id,mtypelabel));
	
CREATE TABLE context_relationship(
	annot_id bigint REFERENCES annotation(annot_id),
	otypelabel char(64),
	context_otypelabel char(64),
	relationship_name char(16),
	is_identifying boolean);
	
CREATE TABLE map(
	annot_id bigint REFERENCES annotation(annot_id),
	mtypelabel char(16),
	attrname char(16),
	mapcond varchar(64),
	mapval varchar(32));
	
CREATE SEQUENCE did_seq;
CREATE TABLE data_annotation(
	did bigint DEFAULT NEXTVAL('did_seq') PRIMARY KEY, 
	annot_id bigint REFERENCES annotation (annot_id),
	dataset_file varchar(256));

CREATE SEQUENCE eid_seq;
CREATE TABLE entity_instance(
	eid bigint DEFAULT NEXTVAL('eid_seq') PRIMARY KEY,
	did bigint REFERENCES data_annotation(did), 
	record_id char(16), 
	etype varchar(64));
		
CREATE SEQUENCE oid_seq;
CREATE TABLE observation_instance(
	oid bigint DEFAULT NEXTVAL('oid_seq') PRIMARY KEY,
	did bigint REFERENCES data_annotation(did), 
	record_id char(16), 
	eid bigint REFERENCES entity_instance(eid),	 
	otypelabel char(32)
	);

CREATE SEQUENCE mid_seq;
CREATE TABLE measurement_instance(
	mid bigint DEFAULT NEXTVAL('mid_seq') PRIMARY KEY,
	did bigint REFERENCES data_annotation(did),
	record_id char(16), 
	oid bigint REFERENCES observation_instance (oid),
	mtypelabel char(32), 
	mvalue varchar(64));
	
CREATE TABLE context_instance(	
	did bigint REFERENCES data_annotation(did), 
	record_id char(16), 
	oid bigint REFERENCES observation_instance (oid),
	context_oid bigint REFERENCES observation_instance (oid), 
	rtype varchar(64));
	