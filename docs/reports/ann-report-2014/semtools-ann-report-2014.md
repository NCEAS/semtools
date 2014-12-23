# Accomplishments

## What are the major goals of the project?
Data for ecological and environmental studies quantify, among other things, the distribution and abundance of organisms; the processes that influence biological populations, communities, and ecosystems; and the environmental and anthropogenic drivers of these processes. Scientists increasingly rely on accessing and analyzing these diverse data collected by cross-disciplinary communities of researchers to achieve synthetic, crosscutting insights into the environment that can address issues of fundamental importance to science and society. 

Despite these needs, discovering these data is difficult. The precision and recall of data searches in data repositories is not satisfactory even at current collection sizes. Data archives like the Knowledge Network for Biocomplexity (KNB), the National Biological Information Infrastructure (NBII) Metadata Clearinghouse, and the Global Change Master Directory (GCMD) rely on semi-structured metadata with fields containing largely natural-language descriptions to provide search and browsing capabilities and to allow human use and interpretation of the data.  These metadata enable simple keyword searches that return results generally related to the topics of interest, but they cannot be used to perform precise searches of the data archives. Ironically data sets with more extensive (natural language) metadata are included in search results simply due to the incidental mention of a term in an ancillary part of the metadata document. These extraneous results decrease the precision of the search, seriously reducing the efficiency in researchers’ finding the data they need. In addition, because natural-language metadata does not generally rely on controlled vocabularies, researchers typically classify their data sets using ad-hoc descriptive terms, reducing recall. Given the number of synonyms and overlapping terms used in scientific disciplines, searches frequently miss relevant data because the search terms do not exactly match the terms used to classify the documents.

The goals of this project are to utilize a semantic model of data and measurements in building new data management tools that can significantly improve data discovery and interpretation within ecology and environmental science.  These tools would include tools for producing semantic metadata and attaching it to ecological data set descriptions, server software to index and reason about this semantic metadata, which in turn is used to build semantic data discovery and integration services that improve scientist's ability to locate, interpret, and repurpose scientific data from large-scale repositories such as the KNB and DataONE.

## What was accomplished under these goals?

### Major Activities
__Activities 2008-2009.__ We started with refinement of our OWL-DL model for scientific observations (OBOE; Figure 2), and development of a prototype semantic search system for Metacat. This prototype was a proof-of-concept for semantic search approaches and allowed us to compare multiple search strategies.  As shown in Figure 1, we added support to Metacat for storing and managing OWL-DL ontologies and semantic annotations, and for reasoning and search services to support different semantic-search strategies.

In our first semantic search implementation, the Jena API was used to access ontologies and ontology terms within Metacat, and Pellet was used to provide reasoning services over these ontologies (e.g., to compute class subsumption hierarchies and to ensure ontologies added to Metacat are consistent).  We also extended Metcat’s XML management capabilities with support for managing semantic annotations.  

In addition to plain-text keyword search, we implemented three different search methodologies to investigate the utility of semantic methods for scientific data discovery: (i) simple term expansion against ontologies to broaden the search terms against the metadata corpus; (ii) term expansion against semantic annotations; and (iii) structured searches that pose queries against the components of an observation described via OBOE. 

__Activities 2009-2010.__ In year 2 we created a new plug-in for the Morpho data management application that allows users to annotate data packages and also search for data that have previously been annotated. Morpho development coincided with authoring a domain-specific ontology that effectively describes data collected at the Santa Barbara Coastal LTER.

The Annotation Plugin for Morpho augments the existing data table view with a tabbed interface that highlights different aspects of the annotation process: Summary, Column, and Context. Given the complexity inherent in formally describing observational data, the team has tried to minimize confusion by keeping the focus of the annotation activity on the actual data table being annotated.

The search interface can be used to create compound nested queries that maximize search precision, and reduce false positive matches. To complement search precision, we use result ranking to maintain broad recall with the “best” or “closest” matches appearing at the beginning of the search results.

__Activities 2010-2011.__ During 2010-2011, advances occurred in four principal areas: 1) development of a revised ontology for the Santa Barbara Coastal LTER data sets; 2) a new ‘MADLIB’ user interface approach to creating semantic data annotations; 3) advances to the semantic query plugin for the Metacat data repository system; and, 4) a new subsystem for materializing OBOE annotations as RDF graphs for use in Open Linked Data applications.

We revised the SBC marine ontology to reflect updates in the OBOE model to support the concept of ‘Measurement Types’, which are combinations of classes that bind together Entity, Characteristic, Measurement Standard, and Protocol classes to form a commonly used composite.  These Measurement Types make is easy to group concepts that are repeatedly used within a project (Figure 4). 

We refined the UI for semantic annotations in Morpho, particularly the user interface components for choosing ontology classes (Figure 5).  For each attribute in a data set, users are asked to fill out a ‘Mad Lib’ style sentence that clarifies the Entity being measured, which characteristics of the Entity are measured, the units of the measurement, and the protocol.

We prototyped a user interface (Figure 6) for submitting semantic queries to Metacat that also includes an option to combine keyword and spatial criteria.  We created a faceted search that exploits ontology subsumption hierarchies to show which queries will produce results in the browse hierarchy.  Users can quickly find all of the data sets that measure a particular Characteristic, or they can find all of the data sets that contain measurements of particular Entities, or to specify data range criteria as part of the query specification (e.g., Plant Mass 'greater than 10' Grams) (Figure 7).
	
We experimented with fully materializing the information contained in metadata documents, data sets, ontologies, and the annotations that link them into an extended RDF graph that is compatible with the principals of the Linked Open Data approach. We found that the graphs containing instance data were sufficiently large to be impractical to query within the constraints of typical triple stores, which is the reason we used our global-as-local mediation design for the semantic search system that we constructed.

__Activities 2011-2012.__ Activities in 2011-2012 focused on continued development, refinement, and refactoring of the OBOE model and extensions, development of ObsDB, a new web-based application for applying semantics to scientific data sets, and outreach activities about OBOE to various groups (see Outreach section). The main project activity in the first half of the project year was refactoring both OBOE and the extension ontologies for marine coastal ecology and for juvenile migrant salmon data to create shared concepts among extension ontologies for common areas such as the representation of space, time, taxa, and methods.  We refactored OBOE and the extensions to allow for a modular set of classes that can be imported independently of one another and that may be usful to various extension ontologies.

ObsDB design and development. S. Bowers had 6 undergraduate student interns working on an initial release version of ObsDB, a new web-based application for applying semantics to scientific data sets.  ObsDB represents an approach to create a unified repository for ontologies, data tables, and semantic annotations.  The goal of this design and development work is to prototype a community-oriented site for management of observational data with explicit semantics attached.

__Activities 2012-2014.__ In no-cost-extension in 2012-2014, we worked to move our prototype semantic annotation and search tools into production usage for data repositories, mainly the KNB Data Repository and the DataONE Federation.  To accomplish this, we needed to overcome implementation barriers that made it difficult to scale semantic search up for use across the hundreds of thousands of data sets available in DataONE.  First, to handle querying at scale and to integrate with the existing DataONE search system, we converted our semantic search system to utilize a SOLR index in combination with reasoners like JENA and Pellet. This allowed us to combine the large scale text¬based index and search systems deployed by KNB and DataONE with the semantic reasoning capabilities exposed by JENA and Pellet (Figure 8).

Second, we reviewed existing standards and developed a new Open Annotation Ontology-based model to replace our original XML-based annotation syntax.  We generated documentation for DataONE review at both the Core Cyber Infrastructure Team meeting in Santa Barbara, CA (June 2014) and the all-hands meeting in Albuquerque, NM (September 2014), and used these materials to drive a review of the proposed semantic search system with the science community.

Third, we developed a new MetacatUI extension for exercising semantic search facets within the existing Metacat search capabilities that utilizes the SOLR-based search API above (Figure 9), and to display the results of annotations to research scientists for review and correction (Figure 10).
  
Finally, we used the extensive natural language metadata about entities and attributes available in the KNB and DataONE repositories to streamline the process of annotating data sets by automatically generating semantic annotations using attribute-level metadata (top box, Figure 8). Names, labels, definitions, and units are used to query BioPortal-hosted ontologies for relevant concept matches.

### Specific Objectives

Based on the goals above, our specific objectives were to produce effective semantic tools for ecological data management, including:

- A refined measurement-driven ontology framework for classifying data sets to improve search
- A semantic annotation toolset that allows scientists to classify research data using that ontology
- A semantic search system that utilizes those annotations to improve precision and recall of measurement-based data searches

We accomplished each of these specific objectives, as outlined in the results and outcomes sections below.

### Significant Results

Our semantic search system in Metacat added the ability to store OWL-DL ontologies in addition to semantic annotations that link data set attributes to ontology terms. Our approach also extended Metacat to improve metadata search: (i) by expanding standard keyword searches with ontology term hierarchies; (ii) by allowing keyword searches to be applied to annotations in addition to traditional metadata; and (iii) by allowing more structured searches over annotations via ontology terms. We compare and contrast these different types of search for a corpus of annotated documents. As data repositories continue to grow, these tools will be instrumental in helping scientists precisely locate and then interpret data for their research needs.

Figure 1 shows the primary components of our semantic-discovery framework. The bottom of Figure 1 consists of two simple, example data sets that contain largely similar information consisting of spatial locations divided into sub-locations (i.e., a plot or quadrat), fertilization treatment information, and weight measurements. Metadata schemes such as the Ecological Metadata Language (EML) provide standard ways of describing the basic structural aspects of data, but the semantics of the data set—the types of entities observed, the characteristics of these entities that were measured, and how these entities were observed in relation to each other—is not parseable. Natural-language metadata alone does not reveal the subtle differences in semantics.

A semantic annotation is a formal structure, which represents a mapping from data set values to ontology instances (i.e., individuals), and an XML-based syntax was used in our first prototype to represent annotation mappings. As shown in Figure 2, we show two annotated attributes that: (i) represent observations of leaf-litter entities; (ii) measure the weight of leaf-litter (although using different weight characteristics); and (iii) use compatible but different measurement units (kilograms and grams). Figure 3 shows a more detailed example of EML attributes (bottom), semantic annotations (middle), and an OBOE ontology extension (top).. 

To evaluate improvements in precision and recall, we tested three search strategies.

In Keyword-Based Term Expansion, we “intercepted” keyword queries and expanded them according to the term hierarchies of stored ontologies. If a given search keyword matched a class name (i.e., as specified by the rdf:label property of the class), then the search was expanded to include the synonyms and subclasses. This form of search alleviates the problem with simple keyword searches of not returning data sets described with synonyms or more specialized terms of the user-entered keywords. The expanded term set was executed against the current Metacat keyword search service. Although this strategy improved recall for documents, it also caused additional false positives due to the addition of keywords, thereby decreasing precision.

In Annotation-Enhanced Term Expansion, semantic annotations allowed individual data set attributes to be linked to one or more ontology classes.  By applying keyword searches only to annotations, search results could potentially improve precision by returning fewer false positives. In annotation-enhanced search, we used the class and all subclasses to find matching annotations. Since the annotation was linked to a specific field within the metadata, data sets containing text comments in other fields were not matched, improving precision.  Moreover, recall was improved due to matches facilitated by descending the ontology’s class hierarchy.

In Observation-Based Structured Query, users  search for data sets via their structure -- observed entities (organism, site, etc.) and the characteristics and standards used to measure them. In an observation-based search, queries were specified by explicitly filling in an observation “template” where ontology classes were given for the observed entity, measurement characteristic, and measurement standard. This type of search had both good recall – hitting all relevant data through appropriate use of term expansion – and good precision by exploiting the structure of OBOE annotations to find exactly the entity, characteristic, and context of interest to the user. 

In developing the Annotation Plugin for Morpho, we found that the inherent complexity of fully describing an observational data table begs for a compact visualization of the annotation in-progress. We developed a succinct fill-in-the-blank summary of each observation and their relationship[s] to one another that provides a formally rigorous and adheres to our strict Entity/Characteristic/Standard/Protocol annotation mapping. We tested it with an extension to the OBOE ontology and applied it to existing real-life marine datasets collected by the Santa Barbara Coastal LTER. The ontology organized and formalized such concepts as the observation Entity (GiantKelp), Characteristic (WetBiomass), Measurement Standard (Gram), and Protocol (wet vs. dry methods). In testing the Morpho plugin for creating semantic annotations, we found that the semantic annotation process itself was relatively straightforward: we produced the XML document that maps ontology classes to data table attributes.  However, the user interface development was challenging because of the complexity of the scientific concepts that we were trying to present in a simple user interface.  The SBC ontology contained hundreds of Entities, Characteristics, Measurement Standards, and Protocols that were interrelated in a complex graph structure that was difficult to present and visualize.   In addition, the user interface needed to show each of the four facets of the OBOE ontology (Entity, Characteristic, MeasurementStandard, Protocol) for each of the attributes in the data set.  Thus, we developed and tested the use of the ‘Mad Lib’ user interface approach shown in Figure 5.  We found that these ‘Mad Lib’ sentences were easy to understand, succinctly presented the ontological information regarding the attribute, and could be compactly displayed when the user selected each data set attribute.  In addition, each field of the MadLib dialog presented the user with a filtered view of the ontology, showing only compatible ontology terms that were relevant for that part of the annotation, thereby significantly reducing the complexity of the ontology shown during annotation.  Even with these advances, we found that users produced varying or inconsistent annotations for the same data files, which confounded their use in semantic search services.

Although the data query feature of the semantic query plugin seemed simple at first glance (selecting all observations with, for example, diameter less than 5 cm.), it was actually complex for the heterogeneous data corpus.  Each of the tens of thousands of data sets had idiosyncratic schemas, and therefore there was no uniform relational model that could be queried to select data values. We used the OBOE ontology as a common global view against which queries were written, and the query subsystem rewrote these queries as appropriate for each local schema.  The annotations thus allowed the native structure of each data set to be maintained while exposing the semantics of the data.

This data query feature represents a simple but powerful form of data union/integration with the Data Manager library (Figure 7).  Currently, knowing that an attribute represents a measurement of a Characteristic of a particular Entity provides the notion that these measurements are the same/compatible, and therefore can be combined. In following Figure 7, one can see that semantic annotations were used to drive local queries against two data sets with completely different schemas, but that the common semantics allowed us to produce a union data product that drew from the corresponding attributes in each of the heterogeneous data sets.  This was a powerful and general data subsetting and integration approach that can be applied to arbitrarily heterogeneous data sets as long as they have shared semantics that are expressed as annotations.  This finding substantiated that our main objectives of improving search were tractable using semantic annotations.

We found that the general issue of having to import large ontologies to use just one or a few concepts from them is a major obstacle to ontology re-use, and one which we determined could be partially alleviated through modularization.  Nevertheless, the problem of cascading imports that have far reaching implications for knowledge modeling still is a significant issue. OBOE was refactored to be comprised of a core model plus a set of extensions for Characteristics, Standards, Space, Time, and various domain Entities.

During our two no-cost extension years, our primary objectives were to transition our previously successful prototyping efforts into robust production systems at the KNB Data Repository and the DataONE federation. 

To overcome scalability barriers, we re-implemented the semantic search system to use the SOLR search system in conjuntion with the previous reasoners that we had employed. By pre-processing the semantic axioms from annotations, we populated a SOLR index with only the key semantic information from the full knowlege model (e.g., by distilling annotations down to a simple concept membership index in SOLR, and making semantic search a simple lookup on a precomputed index).  This allowed us to dynamically handle concept lookahead as users type search terms in the user interface (Figure 9).  This dynamic interface was possible because the semantic relationships were pre-indexed. After extensive usability testing, we produced an effective semantic search UI that can easily be customized and is now incorporated in the Metacat data management application. We also found that the Open Annotation model makes an effective and extensible annotation mechanism in OWL, which makes it simple to fully materialize all the inferred axioms and incorporate these into our SOLR index.
  
Finally, we found that it was possible to generate useful semantic annotations by mining natural language metadata that we already have for a given data set.  While these annotations are not always correct, on average we found that matching the textual metadata to the structured measurement ontologies allowed us to automatically infer measurement types for many data attributes, which in turn improves both recall and precision in measurement search as described above and overcomes the massive barrier that had been present due to the labor required to manually annotate data sets.

### Key outcomes or other achievements

- We produced 13 papers and numerous presentations on the semantics of measurement search, building upon our prior foundational work on the use of ontologies for ecological and environmental science.

- We created 3 open source software tools meeting our specific project objectives.  These tools are distributed from their source code repositories:
    - Metacat semantic indexing and search extensions, with MetacatUI semantic search web interface
	- Morpho semantic annotation extensions
	- Semi-automated annotation generator

- We clarified that the problem of cascading imports has far reaching implications for knowledge modeling  and still is a significant issue for ontologies in general and for OBOE extension ontologies in particular. Overcoming this issue is critical to our community's ability to effectively build modular ontologies that can be re-used..

- We trained nine graduate students during the course of research

- The MetacatUI semantic search system has been incorporated into both the Metacat data repository system, and is being integrated into the DataONE software system.  Thus, it will be in production use in major environmental data repositories in the near future as prodcution releases of these software systems ship.


## What opportunities for training and professional development has the project provided?
Through Semtools, nine students have been supported and worked on the project under the direction of Shawn Bowers at Gonzaga University, in the process gaining valuable training in computer science research: Wesley Saunders, Josie Hunter, and Jay Kudo, along with 6 other student interns during the summer of 2012.

Jay Kudo worked on ObsDB, a system for uniformly storing and querying heterogeneous observational data. Wesley Saunders worked on a Protege plugin that simplifies the development of OBOE-compatible ontologies by providing a simple forms-based user interface for creating ontology subclasses and more complex measurement types. Josie Hunter is working on analyzing KNB data sets to determine and apply attribute similarity measures to assist in semi-automating dataset semantic annotations for datasets. This work helped efficiently provide partial annotations of existing datasets, which is a time-consuming aspect of the semantic software stack we developed.


## How have the results been disseminated to communities of interest?
Outreach activities for the project have principally been through our 13 publications, and through talks at scientific conferences and workshops where we have discussed our approaches to semantically modeling scientific observations and the benefits of doing so, and common use cases. O’Brien is consulting with SBC LTER ecologists and oceanographers in the development of the domain-specific ontology. We also have distributed our software products to the community through our source code repositories for our open source products, including Metacat, MetacatUI, Morpho, and DataONE repositories.

O'Brien introduced OBOE concepts and the Santa Barbara Coastal LTER OBOE extension to the LTER Network community through several venues: the Information Managers' Committee meetings, the Network Newsletter, "Databits", and the working group tasked with developing the Network controlled vocabulary. Her activities included a demonstration of semantics tools in development and an introduction to the mapping of OBOE concepts to attribute definitions in Ecological Metadata Language (EML). She is also involved in an LTER working groups of information managers and scientists tasked with developing a controlled vocabulary for datasets. Early phases of this effort are focused on simple term taxonomies, but considering ontological concepts at this stage will greatly enhance an extension of the LTER vocabulary into a full ontology in the future.

In June 2012, M. O'Brien attended a workshop of the International Long Term Ecological Research (ILTER) Network entitled “Semantic Approaches to Discovery of Multilingual ILTER Data” at the East China Normal University in Shanghai, China. The workshop was hosted by the Chinese Ecosystem Research Network (CERN)/National Ecosystem Research Network of China (CNERN) and brought together information managers from China, Israel, UK, Korea, Taiwan, Japan, and the US. O'Brien presented the OBOE core ontology and the SBC LTER extension, and discussed mechanisms in the ontology that could be applied to international queries. A paper is in preparation.

C. Jones gave two presentations at the Pacific Northwest Aquatic Monitoring Project's data Management Leadership Team meeting on February 21, 2012.  Participants included staff from the Oregon Department of Fish and Wildlife employees, USGS, Ecotrust, and Sitka Pacific Technologies (consultants to PNAMP). The first presentation was an overview of OBOE aimed at introducing the agency managers to observational ontologies. The discussion revolved around the applicability of observational ontologies in cross-agency monitoring efforts. The files are here: https://code.ecoinformatics.org/code/jmx/documents/presentations/20120221-cjones-pnamp-dmlt-oboe-salmon-overview.pdf

The second presentation was a more detailed look at the OBOE ontology itself, and the OBOE-Salmon extension ontology.  This was directed at a more technical audience, and we discussed how the specific concepts are encoded as XML structures in OWL.  The discussion revolved around the effort needed to integrate ontologies into the data workflow for the agencies, and how scientists in the community could and should become involved in defining the classes in the ontology.  The files are here:
https://code.ecoinformatics.org/code/jmx/documents/presentations/20120221-cjones-pnamp-dmlt-oboe-salmon-detail.pdf

Non-conference presentations on Semtools and SONet related work included:

O’Brien, M., Bowers, S., Jones, M., Schildhauer, M. and Leinfelder, B. 2010. SBC Extension of the OBOE Measurement Ontology. LTER Information Managers Committee Meeting, Kellogg Biological Station, Michigan State University, Sept 2010.

Jones, C., Schildhauer, M., Jones, M., O'Brien, M., Leinfelder, M., Bowers, S., Madin, J., Zimmerman, M. 2012. Using semantic technologies to help manage scientific data. Pacific Northwest Aquatic Monitoring Project Data Management Leadership Team meeting, February 21, 2012. 

Jones, C., Schildhauer, M., Jones, M., O'Brien, M., Leinfelder, M., Bowers, S., Madin, J., Zimmerman, M. 2012. An observational ontology for the salmon research community. Pacific Northwest Aquatic Monitoring Project Data Management Leadership Team meeting, February 21, 2012. 

# Products
## Conference Papers and Presentations
- W. Saunders, S. Bowers, M. O'Brien (2011). Protege Extensions for Scientist-Oriented Modeling of Observation and Measurement Semantics. Proc. of the International Workshop on OWL Experiences and Directions (OWLED). San Francisco, California. Status = PUBLISHED;  Acknowledgement of Federal Support = Yes

## Inventions
- Nothing to report.

## Journals
- Ben Leinfelder, Shawn Bowers, Margaret O’Brien, Matthew B. Jones, Mark Schildhauer (2011). Using Semantic Metadata for Discovery and Integration of Heterogeneous Ecological Data.  Proceedings of the Environmental Information Management Conference.   92. Status = PUBLISHED; Acknowledgment of Federal Support = Yes ; Peer Reviewed = Yes ; DOI: doi:10.5060/D2NC5Z4X
- Bowers, Shawn; Cao, Huiping; Schildhauer, Mark; Jones, Matt; Leinfelder, Ben (2010). A semantic annotation framework for retrieving and analyzing observational datasets. Proceedings of the Third Workshop on Exploiting Semantic Annotations in Information Retrieval.   31. Status = PUBLISHED; Acknowledgment of Federal Support = Yes ; Peer Reviewed = Yes ; DOI: doi:10.1145/1871962.1871982
- Chad Berkley, Shawn Bowers, Matthew B. Jones, Joshua S. Madin, Mark Schildhauer (2009). Improving Data Discovery for Metadata Repositories through Semantic Search. International Conference on Complex, Intelligent and Software Intensive Systems.   1152. Status = PUBLISHED; Acknowledgment of Federal Support = Yes ; Peer Reviewed = Yes ; DOI: doi:10.1109/CISIS.2009.122
- David Thau, Shawn Bowers, Bertram Ludäscher (2009). Merging Sets of Taxonomically Organized Data Using Concept Mappings under Uncertainty.  OTM Conferences. 2  1103. Status = PUBLISHED; Acknowledgment of Federal Support = Yes ; Peer Reviewed = Yes ; DOI: doi:10.1007/978-3-642-05151-7_26
- Huiping Cao, Shawn Bowers, Mark P. Schildhauer (2012). Database Support for Enabling Data-Discovery Queries over Semantically-Annotated Observational Data.  Lecture Notes in Computer Science. 7600  198. Status = PUBLISHED; Acknowledgment of Federal Support = Yes ; Peer Reviewed = Yes ; DOI: doi:10.1007/978-3-642-34179-3_7
- O'Brien, M. (2010). Using the OBOE Ontology to Describe Dataset Attributes.  LTER Databits. Fall  . Status = PUBLISHED; Acknowledgment of Federal Support = Yes ; Peer Reviewed = No ; DOI: http://databits.lternet.edu
- S. Bowers, J. Kudo, H. Cao, M. Schildhauer (2010). ObsDB: A system for uniformly storing and querying heterogeneous observational data.  Proc. of the IEEE International Conference on e-Science.   261. Status = PUBLISHED; Acknowledgment of Federal Support = Yes ; Peer Reviewed = Yes ; DOI: doi:10.1109/eScience.2010.24
- Shawn Bowers, Joshua S. Madin, Mark P. Schildhauer (2010). Owlifier: Creating OWL-DL ontologies from simple spreadsheet-based knowledge descriptions.  Ecological Informatics. 5 (1),  19. Status = PUBLISHED; Acknowledgment of Federal Support = Yes ; Peer Reviewed = Yes ; DOI: doi:10.1016/j.ecoinf.2009.08.010

## Licenses
- Nothing to report.

## Other Products
- Software or Netware.
    - Jones, M., Leinfelder B., Schildhauer, M., Bowers, S., O'Brien M. 2011. Semantic extensions to the Metacat data repository software system.  These extensions allow Metacat servers to index and search OWL-DL ontologies and annotations referencing heterogeneous data sources to improve data discovery.
- Software or Netware.
    - Jones, M., Leinfelder B., Schildhauer, M., Bowers, S., O'Brien M. 2011. Prototype semantic extensions to the Morpho metadata edior.  These extensions allow Morpho users to create and edit data set annotations that conform to OWL-DL ontologies and that can be used to improve data discovery.
## Other Publications
## Patents
- Nothing to report.
## Technologies or Techniques
- Nothing to report.
## Thesis/Dissertations
- Nothing to report.
## Websites
- Semtools - http://semtools.ecoinformatics.org
  The Semtools web site is used to describe project goals and accomplishments, disseminate this information to the broader community, and share working documents among project team members.

# Participants/Organizations

## What individuals have worked on the project?
- Jones, Matthew	PD/PI
- Bowers, Shawn	Co PD/PI
- Madin, Joshua	Co PD/PI
- OBrien, Margaret	Co PD/PI
- Schildhauer, Mark	Co PD/PI
- Leinfelder, Benjamin	Other Professional

## What other organizations have been involved as partners?

We have been collaborating with the Scientific Observations Network (SONet), which is an NSF-funded INTEROP project focused on advancing a core semantic model of scientific observations. Semtools is basing much of its development effort on the observation model being evaluated in the SONet project. Initially we are using the OBOE model as the core and building an annotation framework around that model. As the SONet project progresses we hope to produce a generic solution that allows interoperability with different observation models and ontology authoring approaches. The SONet team has provided valuable insight into authoring domain-specific ontologies and defining best-practices regarding ontology construction.

The Moore Foundation has provided funding for collaboration between NCEAS and the Juvenile Salmon Migrant Exchange network (JMX), which is trying to collate and integrate salmon migration data across hundreds of research units in the Pacific Northwest.  In this project, we have a half-time engineer who is developing a salmon migration ontology that can be used to describe all of the data originating from diverse research units spanning local, state, federal, tribal, academic, and non-governmental sectors.  Developing this ontology has played two critical roles in the project.  First, it has tremendous heuristic value in clarifying the subtle, nuanced differences in measurements being taken across projects.  Second, it is being used to annotate a collection of data sets from the Washington Department of Fish and Wildlife, which in turn allows the Semtools project to demonstrate the power of semantic search and semantic data integration for these diverse institutions.

DataONE Data Integration and Semantics Working Group. Co-PIs Schildhauer and O'Brien and PI Jones are collaborating with DataONE's semantics working group to develop an interoperable semantic data discovery application that focuses on ecohydrology as a use case.  Collaborators from Semtools, SONet, DataONE, CUASHI, and other projects are developing a semantically integrated data query system that illustrates the power of semantics in making heterogeneous data accessible for cross-cutting science use cases.  The ecohydrology use case will draw together water chemistry and biodiversity data from a variety of sources, including the CUASHI HIS system, the USGS NWIS system, the Santa Barbara Coastal LTER, and other sites.

EarthCube Semantics and Ontologies Activity. Semtools co-PI Schildhauer has established a collaboration with geoscience semantics researchers as part of the EarthCube Semantics and Ontologies Working Group.  This group is developing a vision and roadmap for the development and utility of semantics technologies within the geosciences.  Schildhauer helped to co-author the EarchCube Roadmap for semantics (see publications list for citation).

Semtools participants (Jones, Schildhauer, Bowers) have created the Joint Working Group on Observational Data Semantics with other participants from the DataONE, Data Conservancy, and SONet projects.  The purpose of this Joint Working Group is to identify and pursue synergies between the projects in observational data semantics.  We have held two workshops of the participants, which each resulted in a shared understanding of our varied models of observational data, as well as a joint commitment to compatible development.  Future activities of the joint working group will include an emphasis on a core mode for observational data semantics, an exchange syntax for moving data and their associated semantics across systems, and demonstration prototypes of interoperability that arises from this work.

# Impact

## What is the impact on the development of the principal discipline(s) of the project?
Through our work on Semtools, we have demonstrated improvements in the effectiveness of data discovery for large, heterogeneous data collections such as the Knowledge Network for Biocomplexity (KNB) and DataONE, an NSF-funded DataNet partner.  These advances have been possible through the use of a semantic model of scientific observations (Extensible Observation Ontology) and an annotation language that is used to map relational data sources to the concepts in OBOE.  The system that we developed will form the basis for a production semantic search and annotation system that will be deployed wihtin DataONE and will have broad applicability in the ecological and environmental sciences.

The other major impact was on ontology development within the environmental sciences.  Our development of the OBOE model, and our prototype work on using ontologies for data annotation, demonstrated how difficult it is to reach semantic clarity about envirnmental measurements.  The effort resulted in changes to other standards such as the Observations and Measurements standard, and had a large impact on the development of the Semantic Sensor Netork (SSN) ontology that was initially developed by the W3C.  The current movement within the ecological sciences to develop ontologies for organizing and formalizing what was observed and how provides the semtools team with a good opportunity to exchange ideas about creating these ontologies. Our initial work with OBOE and the Morpho Annotation Plugin has illuminated questions about how disparate ontologies must be unified without having to accept the axioms from any particular model, which is still a challenge in ontology engineering. 

Ultimately, as our annotation system is put into prodcution in the KNB and DataONE, we will have a large impact on the accessibility and utility of environmental data through as locating and synthesizing heterogeneous data sources will be more efficient, and downstream analysis more accurate.

## What is the impact on other disciplines?
The relative newness of knowledge representation and the use of ontologies to express and formalize information in a way that machines can ‘understand’ puts our real-life use of the technology at the forefront of the art. We are involved in the OWL API user community – a forum for both providing and soliciting support for the rapidly evolving software. Similarly, our extensive use of Protégé increases the user base directly and indirectly as we encourage collaborators to view and author domain ontologies within this application.  Our work on materializing scientific data sets as large OWL graphs using conventions from the Linked Open Data community also contributes to an understanding of the scalability of linked data approaches that transcends disciplines.

## What is the impact on the development of human resources?

## What is the impact on physical resources that form infrastructure?
Although the Semtools project itself will not produce physical infrastruture per se, its software will be deployed on the KNB Data Repository and the DataONE federation data servers, thereby having a large impact on the data search facilities that are available in the US and globally.

## What is the impact on institutional resources that form infrastructure?
Semtools semantic search technologies will have a major impact on the utility of environmental data repositories, and we expect it will help significantly improve the ability of non-profit operations like the KNB and DataONE to transition into useful, sustainable, and effective virtual organizations.  The utility of effective data management is growing in the environmental sciences, and Semtools software will enable data repositories to meet the needs of these virtual orgnaizations.  In addition, government agencies, including NSF, NOAA, NASA, and are increasingly under pressure to provide effective access to open data.  For NSF in particular, data heterogeneity is a huge barrier to distribution of open data resulting from NSF research. The semantic annotation and search systems that we have produced will be able to help NSF and other agencies to meet open data mandates.

## What is the impact on information resources that form infrastructure?
The project is helping to build the extensive Knowledge Network for Biocomplexity (KNB) repository, which provides tens of thousands of data sets for use in research and educational contexts.  Data from the KNB will become more accessible as the semantic search facilities that we have developed become incorporated into the production Metacat software used by the KNB.  This will enable educators and researchers to more readily access KNB data and therefore facilitate science and education advances in many disciplines. In addition, DataONE has also decided to incoporate Semtools semantic search software into the DataONE data search systems.  DataONE is a major information integrator for the whole NSF DataNet program, as it has provided a common discovery service across all 4 of the active DataNet partners (Minnesota Population Center (MPC), DataNet Federation Consorium( DFC), SEAD, and DataONE), and a total of 25 disctinct data partners and networks (including other data providers such as LTER, Dryad, the ORNL DAAC repository, the Alaska Ocean Observing System, and others).  As a consequence, Semtools software will have a large network effect as it gets applied to the hundreds of thousands of data sets available through the DataONE federation. 

## What is the impact on technology transfer?
- None

## What is the impact on society beyond science and technology?
Knowledge about science and the progress of science is critical to an effective society.  Advances in the Semtools project are producing new techniques for clarifying the content and meaning of scientific observations data to make it useful for tackling cross-cutting issues that are important to society.  The data that are exposed in this way become useful to many communities, including local governments and resource management agencies, non-profit organizations focused on conservation issues, and educators interested in exposing students to science approaches to societal issues.




