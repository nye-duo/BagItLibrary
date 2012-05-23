BagIt Library for StudentWeb & Duo-DSpace
=========================================

Introduction
------------

This library enables the packaging of files and metadata from StudentWeb into Bags and the unpackaging of these Bags for
 use in Duo-DSpace.

The Bags have the following structure:

	<base directory>/
	 |   bagit.txt
	 |   manifest-md5.txt
	 |   tagmanifest-md5.txt
	 \--- data/
		   \--- final
				 |   [final version files]
		   \--- supporting
				 |   [supporting files]
		   \--- licence/
				 |   licence.txt
		   \--- metadata/
				 |   metadata.xml
	 \--- tagfiles/
		   |   supporting.access.txt
		   |   formats.txt
		   |   final.sequence.txt
		   |   supporting.sequence.txt

This is as per the technical design documentation available at:

	https://docs.google.com/document/d/1mR5r3XvCkM2oLy08hDVMdjUGmdAh0lChVcxtAkvzmyI/edit

	(see Annex B)



Build/Install
-------------

###LOC BagIt Library

This library depends on the Library of Congress BagIt Library version 4.1 which is not currently available in the Maven
central repository.  It can be downloaded from:

    http://sourceforge.net/projects/loc-xferutils/files/loc-bil-java-library/4.1/bagit-4.1-src.zip/download

Once downloaded it can be installed into the local maven repository with

	mvn install

###BagItLibrary for Student Web

Once these non-maven-central-repo dependencies have been installed, then it's possible to compile this library with:

    mvn clean package


Bags
----

###Construction

A BagIt object must be constructed with the path to a zip file; this path may point to an existing file from which
the library should construct an object, or it may point to a non-existant file, in which case the BagIt will be
constructed in-memory and then ultimately serialised to that location.

	BagIt bag = new BagIt("/path/to/bag.zip");

###Adding Files

The library supports the semantics required by StudentWeb and DUO for the structure of the BagIt objects created.
As such there are a number of methods which can be used to add content files to the Bag, so that they appear in
the correct place in the package:

	addFinalFile
	addSupportingFile
	addMetadataFile
	addLicenceFile

Each of these maps on to the appropriate part of the package structure as defined above.  For example, addFinalFile
will add files to the data/final directory.


####addFinalFile

Add a File object to the BagIt in the specified sequence position as a final file

	public void addFinalFile(File file, int sequence)
	public void addFinalFile(File file)

If no sequence is supplied, then the file will be appended to the list of existing files with an automatically
assigned sequence number.

When the BagIt is eventually serialised, the sequences for these files will be written to

	tagfiles/final.sequence.txt


####addSupportingFile

Add a File object to the BagIt in the specified sequence position as a supporting file, with the specified
level of access.

	public void addSupportingFile(File file, int sequence, String access)
	public void addSupportingFile(File file, String access)

If no sequence is supplied, then the file will be appended to the list of existing files with an automatically
assigned sequence number.

The access is a free string, but SHOULD be either "open" or "closed" for suitable interpretation by the DUO
implementation.

When the BagIt is eventually serialised, the sequences for these files will be written to

	tagfiles/supporting.sequence.txt


####addMetadataFile

Add a File object to the BagIt which will represent the metadata for the packaged item.

	public void addMetadataFile(File file)

When the BagIt is serialised, this file will be written to

	data/metadata/metadata.xml

You may instead wish to add metadata using the Metadata object - see below.


####addLicenceFile

Add a File object to the BagIt which will represent the licence for the packaged item

	public void addLicenceFile(File file)

When the BagIt is serialised, this file will be written to

	data/licence/licence.txt


###Adding Metadata Explicitly

Metadata can be added by giving the BagIt object a metadata file as described above, using

    public void addMetadataFile(File file)

But you may also add metadata using the Metadata object:

	public void addMetadata(Metadata metadata)

This will have the same overall effect as adding a metadata file, but guarantees that the file format is in-line with
the BagIt specification for the StudentWeb/DUO integration.  See the section below on Metadata for more details.


###Serialising

Once you have constructed a BagIt object which represents the content you wish to pack, it can be serialised.  The
serialisation takes place at the file location provided in the constructor, and will always output a ZIP file

	// if /path/to/bag.zip does not exist, the Bag will only exist in-memory
	BagIt bag = new BagIt("/path/to/bag.zip");

	// add all the content to the Bag.  This happens in-memory
	bag.addFinalFile(finalFile)
	bag.addSupportingFile(supportingFile)
	bag.addMetadata(metadata)
	bag.addLicence(licence)

	// write the contents of the Bag to /path/to/bag.zip
	bag.writeToFile()

In the process of writing the file, all the relevant tagfiles and manifests will be automatically created.

###Reading from the Bag

When extracting content from the bag, a new BagIt object should be constructed over an existing ZIP file.  Once this
has been done we can use the following methods to read the data from the bag

	getSequencedFinals
	getSequencedSecondaries
	getMetadataFile
	getLicenceFile

####getSequencedFinals

This returns a TreeMap (which is intrinsically ordered by the natural ordering of the key object) of the final files
in the BagIt.

	public TreeMap<Integer, BaggedItem> getSequencedFinals()

The value of the map at any sequence point is a BaggedItem which is a simple object which wraps the filename, the
format, the sequence number and an input stream for retrieving the content.  The key in the TreeMap is the sequence
number of the item, so they can easily be processed in intended sequence order by the caller.


####getSequencedSecondaries

This returns a TreeMap (which is intrinsically ordered by the natural ordering of the key object) of the final files
in the BagIt.  The accessRights argument filters the returned BaggedItems by whether they support that access
condition (the values of the accessRights argument SHOULD be "open" or "closed").

	public TreeMap<Integer, BaggedItem> getSequencedSecondaries(String accessRights)

The value of the map at any sequence point is a BaggedItem which is a simple object which wraps the filename, the
format, the sequence number and an input stream for retrieving the content.  The key in the TreeMap is the sequence
number of the item, so they can easily be processed in intended sequence order by the caller.


####getMetadataFile

This returns a BaggedItem representation of the Metadata file in

	data/metadata/metadata.xml

A BaggedItem is a simple object which wraps the filename, the format, the sequence number and an input stream for retrieving the content.


####getLicenceFile

This returns a BaggedItem representation of the Licence file in

	data/licence/licence.txt

A BaggedItem is a simple object which wraps the filename, the format, the sequence number and an input stream for retrieving the content.


###Other operations

The BagIt object has a number of other operations (see the Javadoc) which are not documented here.  They are
mostly used internally or by the StudentWeb library to integrate the BagIt with SWORDv2.


Metadata
--------

###Introduction

The metadata format for use in the BagIt package when transferring content from StudentWeb to DUO is defined here:

	https://docs.google.com/spreadsheet/ccc?key=0Ah-HG8-1YyIldEZmbXFuQWU1bXNWWUJ3N3Fud3hfUlE

The Metadata object in this library provides an interface to create and read documents which meet this specification
to ensure that they are correctly constructed for the package.

###Construction

A blank metadata object can be constructed thus:

	Metadata metadata = new Metadata();

If you have a pre-existing XOM XML Element, the metadata object can be initialised around it thus:

	Metadata metadata = new Metadata(xomElement);

###Static Constants

The metadata object provides a list of static constants which represent the metadata fields supported by the
StudentWeb/DUO schema.  These are:

	Metadata.NAME
    Metadata.GIVEN_NAME
    Metadata.FAMILY_NAME
    Metadata.STUDENT_NUMBER
    Metadata.UID
    Metadata.FOEDSELSNUMMER
    Metadata.POSTAL_ADDRESS
    Metadata.EMAIL
    Metadata.TELEPHONE_NUMBER
    Metadata.SUBJECT
    Metadata.SUBJECT_CODE
    Metadata.SUBJECT_TITLE
    Metadata.UNITCODE
    Metadata.UNIT_NAME
    Metadata.EMBARGO_TYPE
    Metadata.EMBARGO_END_DATE
    Metadata.GRADE
    Metadata.TITLE
    Metadata.LANGUAGE
    Metadata.ABSTRACT
    Metadata.TYPE

These MUST be used when setting metadata fields, as the Metadata object will be able to correctly interpret the
field names and namespaces, and ensure that the resulting document is valid.

###Adding Metadata

There are 3 methods for adding metadata

	addField
	addSubject
	setEmbargo

"addField" is a generic method which should be used in all cases except when setting the subject or the embargo metadata,
in which case the relevant one of the other two methods should be used.

####addField

Add a field to the metadata document with the given value (and optionally, language).

	public void addField(String fieldName, String value)
	public void addField(String fieldName, String value, String language)

For example:

	Metadata metadata = new Metadata();
	metadata.addField(Metadata.TITLE, "The title")
	metadata.addField(Metadata.TITLE, "Titelen", "nob");

####addSubject

Add a subject code and its associated title to the metadata document

	public void addSubject(String code, String title)

For example

	Metadata metadata = new Metadata();
	metadata.addSubject("A001", "Computing");
	metadata.addSubject("B023", "Social Sciences");

####setEmbargo

Set the embargo metadata in the document

    public void setEmbargo(String type, Date date)

For example:

	Metadata metadata = new Metadata();
	metadata.setEmbargo("restricted", new Date());


###Reading Metadata

There are 2 methods for retrieving metadata from the object

	getField
	getSubjects

Note that there is no getEmbargo method, as this can be achieved by

	String end = metadata.getField(Metadata.EMBARGO_END_DATE).get(0);
	String type = metadata.getField(Metadata.EMBARGO_TYPE).get(0);

####getField

Get the list of values which are in the metadata document for the requested field

	public List<String> getField(String fieldName)
	public List<String> getField(String fieldName, String language)

If a language is supplied, only fields which have the xml:lang property set to that language will be returned.

For example:

	Metadata metadata = new Metadata(xomElement);
	List<String> titles = metadata.getField(Metadata.TITLE);
	List<String> nobTitles = metadata.getField(Metadata.TITLE, "nob");


####getSubjects

Get a list of 2 element String arrays which represent the subject code and subject title for each subject in
the metadata document.

    public List<String[]> getSubjects()

The List is a list of all of the subjects, and each String[] is a 2 element array where the first element is the
code and the second element is the title.

For example:

	Metadata metadata = new Metadata(xomElement);
	List<String[]> subjects = metadata.getSubjects();
	for (String[] codeTitle : subjects)
	{
		String code = codeTitle[0];
		String title = codeTitle[1];
	}

###Serialisation

To serialise the metadata document, just call toXML():

	Metadata metadata = new Metadata();
	metadata.addField(Metadata.TITLE, "The title")
	String xml = metadata.toXML();

If you wish to access the inner XOM Element to serialise in your own way, you can just use

	Element element = metadata.getElement();

