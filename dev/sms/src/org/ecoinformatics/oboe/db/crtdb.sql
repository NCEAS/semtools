CREATE SEQUENCE did_seq;
CREATE TABLE data_annotation(
	did bigint DEFAULT NEXTVAL('did_seq') PRIMARY KEY,
	dataset_file varchar(256),
	with_rawdata boolean,
	annot_uri varchar(256));
		
CREATE TABLE observation_type(
	annot_id bigint REFERENCES data_annotation(did),
	otypelabel char(16), 
	ename char(32), 
	is_distinct boolean,
	PRIMARY KEY (annot_id,otypelabel));
	
CREATE TABLE measurement_type(
	annot_id bigint REFERENCES data_annotation(did),
	mtypelabel char(16), 
	otypelabel char(16),
	iskey boolean,
	characteristic char(64), 
	standard varchar(64), 
	protocal varchar(256),
	PRIMARY KEY (annot_id,mtypelabel));
	
CREATE TABLE context_type(
	annot_id bigint REFERENCES data_annotation(did),
	otypelabel char(16),
	context_otypelabel char(16),
	relationship_name varchar(16),
	is_identifying boolean);
	
CREATE TABLE map(
	annot_id bigint REFERENCES data_annotation(did),
	mtypelabel char(16),
	attrname varchar(32),
	mapcond varchar(64),
	mapval varchar(32));

CREATE TABLE observation_instance(
	oid bigint PRIMARY KEY,
	did bigint REFERENCES data_annotation(did), 
	record_id bigint,
	etype varchar(64),
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
	
CREATE TABLE ei_compress(
	did bigint,
	eid bigint, 
	compressed_record_id bigint);
	