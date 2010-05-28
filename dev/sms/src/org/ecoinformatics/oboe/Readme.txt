
Readme for using package "org.ecoinformatics.oboe".
Last changed: March 18, 2010, Thursday

===============
Package oboe jar file: 
1. Run OboeBuild.xml
   copy_lib_file
   oboejar
2. Run .sh file in dev/sms/executable/*.sh

===============
This package contains content for the following parts with separate entrances. 
1.  Materialize a plain text file to OBOE file
    Entrance: MaterializaDB.java
    The detailed parameter specification to use this function, see the comments in the main() function of this class
    
2.  Generate synthetic data for OBOE materialization test
    Entrance: SyntheticDataGenerator.java 
    The detailed parameter specification to use this function, see the comments in the main() function of this class
    
    Test cases: 
    (1) Input 
    	Annotation specification files: /dev/oboedb/*-annot-spec.txt
    (2) Output files
        Annotation files: /dev/oboedb/*-annot.xml
        Data files: /dev/oboedb/*-n*-data.txt 

3.  Validate whether the data satisfy the annotation specification.
    Entrance: SyntheticDataValidator.java
	The detailed parameter specification to use this function, see the comments in the main() function of this class

	Test cases:
	(1) Input: 
	    Annotation specification files: /dev/oboedb/*-annot-spec.txt
	    Data files: /dev/oboedb/*-n*-data.txt 
	(2) Output:
	    VALID	    
	    INVALID
	    
===============
IDE configuration (date: March 17, 2010, Wed.) 
Make sure to configure your classpath correctly (due to Ben's changes)  
1. from classpath, remove jar:
   org.semanticweb.owl.owlapi.jar
2. from classpath, add jar: 
   owlapi-bin.jar
   owlapi-src.jar
   jgraphx.jar
   These jars are under /dev/sms/lib 