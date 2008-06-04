Semtools document and code repository
-------------------------------------

This is the document and source code repository for joint efforts
developing ontology-based data and workflow tools. These projects
currently include SEEK (expiring), BDI (to-be-named), and SONET.

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
       |   |-- project1
       |   |-- project2
       |   |-- ...
       |
       |-- docs
       |   |-- presentations
       |
       |-- meetings
           |-- 20070201-mtg (as an example)

This is just an example -- feel free to extend or modify as needed.

More information on using Subversion, including on Subversion branching, is
available in the Subversion Book: http://svnbook.red-bean.com/ .
