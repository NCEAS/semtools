DELETE FROM ei_compress;
DELETE FROM context_instance;
DELETE FROM measurement_instance;
DELETE FROM observation_instance;
DELETE FROM entity_instance;

DELETE FROM map;
DELETE FROM context_type;
DELETE FROM measurement_type;
DELETE FROM observation_type;

DELETE FROM data_annotation;
ALTER SEQUENCE did_seq RESTART WITH 1;