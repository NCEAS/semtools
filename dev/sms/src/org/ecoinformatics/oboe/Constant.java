package org.ecoinformatics.oboe;

/**
 * @author cao
 * 
 * This is used for testing purpose
 * It should be changed to a properties file.
 * But at the testing stage, I don't want to spend too much time on figuring the parameters.
 */
public class Constant {
	public static String inputUriPrefix = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/examples/";
	public static String outputUriPrefix = "https://code.ecoinformatics.org/code/semtools/trunk/dev/sms/oboedb/";
	public static String localUriPrefix = "/Users/cao/DATA/SONET/svntrunk/semtools/dev/sms/oboedb/";	
	
	//File default suffix
	public static String C_EML_FILE_SUFFIX ="-eml.xml";
	public static String C_ANNOT_SPEC_FILE_SUFFIX = "-annot-spec.txt";
	public static String C_ANNOT_FILE_SUFFIX = "-annot.xml";
	public static String C_DATA_FILE_SUFFIX = "-data.txt";
	public static String C_OUT_CSV_FILE_SUFFIX ="-oboe.csv";
	public static String C_OUT_RDF_FILE_SUFFIX ="-oboe.rdf";
	
	public static String C_DATASET_SEPARATOR = ",";	
	
	//Used in annotation specifier to write to annotation file after reading annotations from the specifications files
	public static String ANNOTATION_DEFAULT_RELATIONSHIP_URI = "oboe#";
	public static String ANNOTATION_DEFAULT_RELATIONSHIP_NAME = "HasA";
	public static String ANNOTATION_DEFAULT_ONTOLOGY = "testOnto";
}
