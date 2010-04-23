CREATE TABLE entity_instance(record_id varchar(256), eid bigint, etype varchar(64));
CREATE TABLE observation_instance(record_id varchar(256), eid bigint, oid bigint, otype varchar(64));
CREATE TABLE measurement_instance(record_id varchar(256), oid bigint, mid bigint, mlabel varchar(64), mvalue varchar(64));
CREATE TABLE context_instance(record_id varchar(256), context_record_id varchar(256), oid bigint, context_oid bigint, rtype varchar(64));

CREATE TABLE observation_type(annot_id varchar(256), olabel varchar(64), ename varchar(64), is_distinct boolean);
CREATE TABLE measurement_type(
	mtype char(16), 
	olabel varchar(64),
	iskey boolean,
	characteristic char(64), 
	standard char(16), 
	protocal varchar(256));