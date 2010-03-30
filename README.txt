Semtools document and code repository
-------------------------------------

This is the document and source code repository for joint efforts
developing ontology-based data and workflow tools. These projects
currently include SEEK (expiring), *BDI (to-be-named)*, Semtools, 
and SONET.

Organization of the repository
------------------------------

All documents and source code should be checked into the 'trunk'
directory unless you are explicitly working on a branch.  Under no
circumstances should check ins be made to additional directories under
the root of the repository (branches, tags, and trunk are the only
allowed directories, by convention).

Please be careful when organizing the repository trunk to keep it
understandable to the rest of us.  Most importantly, try to keep the
top-level directories organized and to a minimum.  A structure that
has worked for other projects is:

semtools
   |
   |-- trunk
       |
       |-- dev 
       |   |-- oboe: ontology (oboe, sbc, gce) OWL files (mainly contributed by Margaret Obrien)
       |   |-- owlifier: read excel files???  (mainly maintained by Shawn Bowers)
       |   |-- sms: sms project source codes
       |       |-- src/jp.gr.java_conf.tame.swing.table (maintained by Ben Leinfelder)
       |       |-- src/org.ecoinformatics
       |       |   |-- oboe: oboe materialization (maintained by Huiping Cao)
       |       |   |   |--Readme.txt: Guidelines in using org.ecoinformatics.oboe package
       |       |   |                  Further details of this package is in this file.
       |       |   |
       |       |   |-- sms: semtools main plug-ins (maintained by Ben Leinfelder)
       |       |-- oboedb: test data for materialize DB (mainly maintained by Huiping Cao)
       |
       |-- docs
       |   |-- algdraft: algorithms for materializing OBOE database (mainly maintained by Huiping Cao)
       |   |-- design: design documents for annotation (maintained by Ben Leinfelder)
       |   |-- meetingnotes: some selective meeting notes (maintained by different people)
       |   |   |-- whiteboards: Marratech whiteboard screen shots (mainly maintained by Ben Leinfelder)
       |   |
       |   |-- oboe-guide: how to use OBOE model (mainly maintained by Shawn Bowers) 
       |   |-- presentations: the published presentation materials for this project (maintained by different people)
       |   |-- pubs: publications for this project (maintained by different people)
       |   |-- reports: reporting materials for this project (mainly maintained by Matt Jones)
       |
       |-- use_cases 
       |   |-- background_material (mainly maintained by Margaret Obrien)

This is just an example -- feel free to extend or modify as needed.

More information on using Subversion, including on Subversion branching, is
available in the Subversion Book: http://svnbook.red-bean.com/ .
