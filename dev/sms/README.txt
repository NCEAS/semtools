Instructions for building and running Morpho w/ Semtools plugin
---------------------------------------------------------------
1. Get projects from SVN such that "morpho" and "semtools" are sibling directories like so:

workspace/
	morpho/
	semtools/

1a. Using these commands:
	
svn co https://code.ecoinformatics.org/code/morpho/trunk/ morpho
svn co https://code.ecoinformatics.org/code/semtools/trunk/ semtools

2. Build/install/run from semtools dev folder (this directory):

cd semtools/dev/sms
ant morpho

2a. Other useful ant targets:
ant compile
ant install

3. Additional info:
	-The build process will take care of downloading other project dependencies from "eml" and "utilities". 
	-eml/ will be another sibling directory, whereas utilities is hidden in the morpho/build/ directory.
 