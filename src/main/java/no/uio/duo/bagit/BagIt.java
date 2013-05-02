package no.uio.duo.bagit;

/*
Copyright (c) 2012, University of Oslo

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
* Neither the name of the University of Oslo nor the
names of its contributors may be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF OSLO BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import gov.loc.repository.bagit.*;
import gov.loc.repository.bagit.impl.FileBagFile;
import gov.loc.repository.bagit.impl.StringBagFile;
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import gov.loc.repository.bagit.writer.impl.ZipWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Main class which manages interactions with a StudentWeb formatted BagIt file.
 *
 * This allows the BagIt packages to be constructed from files on disk, or for a
 * zipped BagIt to be read and opened.
 *
 * The StudentWeb Bag format is as follows:
 *
 * <pre>
 * base directory/
 * |   bagit.txt
 * |   manifest-md5.txt
 * |   tagmanifest-md5.txt
 * \--- data/
 *      \--- final
 *          |   [final version files]
 *      \--- supporting
 *          |   [supporting files]
 *      \--- licence/
 *          |   licence.txt
 *      \--- metadata/
 *          |   metadata.xml
 *      \--- tagfiles/
 *          |   supporting.access.txt
 *          |   formats.txt
 *          |   final.sequence.txt
 *          |   supporting.sequence.txt
 *</pre>
 */
public class BagIt {

    // our BagFactory
    Bag theBag;
    BagFactory bagFactory = new BagFactory();
    Manifest manifest;
    Manifest tagmanifest;

    String formats = "";
    String finalSequence = "";
    String supportingSequence = "";
    String supportingAccess = "";

    HashMap<String, String> formatMap;
    HashMap<String, String> accessMap;

    int finalSequenceCounter = 1;
    int supportingSequenceCounter = 1;

    File bagFile = null;

    /**
     * Create a BagIt object around a directory specified at the filePath
     *
     * @param filePath  Path to BagIt structure
     * @throws IOException
     */
    public BagIt(String filePath)
            throws IOException
    {
        this(new File(filePath));
    }

    /**
     * Create a BagIt object around a file object provided
     *
     * @param file  File object representing the BagIt structure
     * @throws IOException
     */
    public BagIt(File file)
            throws IOException
    {
        this.bagFile = file;
        if (file.exists())
        {
            // load the bag
            theBag = bagFactory.createBag(file);
            setManifestsAndTagfiles();
        }
        else
        {
            // create the bag
            theBag = bagFactory.createBag(BagFactory.Version.V0_97);

            // create our manifests
            manifest = theBag.getBagPartFactory().createManifest(ManifestHelper.getPayloadManifestFilename(Manifest.Algorithm.MD5, theBag.getBagConstants()));
            tagmanifest = theBag.getBagPartFactory().createManifest(ManifestHelper.getTagManifestFilename(Manifest.Algorithm.MD5, theBag.getBagConstants()));
        }
    }

    /**
     * Add a file to the Bag, which is a final version file of the item (see the class documentation regarding
     * structure of the Bag).
     *
     * @param file  File object to add to the Bag
     */
    public void addFinalFile(File file)
    {
        // if we don't have a sequence number
        addFinalFile(file, null, -1);
    }

    /**
     * Add a file to the Bag, which is a final version file of the item (see the class documentation regarding
     * structure of the Bag).  Give it the specified sequence number in the list of final files
     *
     * @param file  File object to add to the Bag
     * @param sequence  position in the sequence of final files in the Bag
     */
    public void addFinalFile(File file, int sequence)
    {
        this.addFinalFile(file, null, sequence);
    }

    /**
     * Add a file to the Bag, which is a final version file of the item (see the class documentation regarding
     * structure of the Bag).  Give it the specified mime type and sequence number in the list of final files
     *
     * @param file  File object to add to the Bag
     * @param mimeType  Mimetype of the file object.  If this is null, we will attempt to guess
     * @param sequence  position in the sequence of final files in the Bag.  If this is -1 we will just add it to the end of the current list
     */
    public void addFinalFile(File file, String mimeType, int sequence) {

        // check if we have a sequence
        if (sequence >= 0)
        {
            // set our sequence counter to the right value
            finalSequenceCounter = sequence;
        }

        // data final directory
        String dataFinal = "data/final/" + file.getName();

        // add the file
        theBag.putBagFile(new FileBagFile(dataFinal, file));

        // add the format to tagfiles/formats.txt
        if (mimeType == null)
        {
            mimeType = new MimetypesFileTypeMap().getContentType(file);
        }
        formats = formats + mimeType + "\t" + dataFinal + "\n";

        // add the file to the final.sequence.txt
        finalSequence = finalSequence + finalSequenceCounter + "\t" + dataFinal + "\n";

        // increment the sequence counter
        finalSequenceCounter++;

        // generate the checksum
        String checksum = MessageDigestHelper.generateFixity(new FileBagFile(dataFinal, file).newInputStream(), Manifest.Algorithm.MD5);

        // add file to payload manifest
        manifest.put(dataFinal, checksum);
    }

    /**
     * Add a file to the Bag, which is supporting material for the item (see the class documentation regarding
     * structure of the Bag).  Give it the access type specified.
     *
     * @param file  File object to add to the Bag
     * @param access    Access conditions of the item.  Should be "open" or "closed"
     */
    public void addSupportingFile(File file, String access)
    {
        // if we don't have a sequence number
        addSupportingFile(file, null, -1, access);
    }

    /**
     * Add a file to the Bag, which is supporting material for the item (see the class documentation regarding
     * structure of the Bag).  Give it the access type specified, and the position specified by the sequence
     * in the existing list of supporting files
     *
     * @param file  File object to add to the Bag
     * @param sequence  position in the sequence of final files in the Bag.  If this is -1 we will just add it to the end of the current list
     * @param access    Access conditions of the item.  Should be "open" or "closed"
     */
    public void addSupportingFile(File file, int sequence, String access)
    {
        // if we don't have a sequence number
        addSupportingFile(file, null, sequence, access);
    }

    /**
     * Add a file to the Bag, which is supporting material for the item (see the class documentation regarding
     * structure of the Bag).  Give it the mimetype and access type specified, and the position specified by the sequence
     * in the existing list of supporting files
     *
     * @param file  File object to add to the Bag
     * @param mimeType  Mimetype of the file object.  If this is null, we will attempt to guess
     * @param sequence  position in the sequence of final files in the Bag.  If this is -1 we will just add it to the end of the current list
     * @param access    Access conditions of the item.  Should be "open" or "closed"
     */
    public void addSupportingFile(File file, String mimeType, int sequence, String access)
    {
        // check if we have a sequence
        if (sequence >= 0)
        {
            // set out sequence counter to the right value
            supportingSequenceCounter = sequence;
        }

        // data supporting directory
        String dataSupporting = "data/supporting/" + file.getName();

        // add the file
        theBag.putBagFile(new FileBagFile(dataSupporting, file));

        // add the format to tagfiles/formats.txt
        if (mimeType == null)
        {
            mimeType = new MimetypesFileTypeMap().getContentType(file);
        }
        formats = formats + mimeType + "\t" + dataSupporting + "\n";

        // add the file to the supporting.sequence.txt
        supportingSequence = supportingSequence + supportingSequenceCounter + "\t" + dataSupporting + "\n";

        // increment the sequence
        supportingSequenceCounter++;

        // add the file tagfiles/supporting.access.txt as access (open|closed)
        supportingAccess = supportingAccess + access + "\t" + dataSupporting + "\n";

        // generate the checksum
        String checksum = MessageDigestHelper.generateFixity(new FileBagFile(dataSupporting, file).newInputStream(), Manifest.Algorithm.MD5);

        // add file to payload manifest
        manifest.put(dataSupporting, checksum);
    }

    /**
     * Add the provided metadata object to the Bag in the appropriate file format
     *
     * @param metadata  Metadata object
     */
    public void addMetadata(Metadata metadata)
    {
        String mdxml = metadata.toXML();
        StringBagFile sbf = new StringBagFile("data/metadata/metadata.xml", mdxml.getBytes());
        this.addMetadataBagFile(sbf);
    }

    /**
     * Add an appropriately formatted metadata file as the metadata file for this Bag
     *
     * @param file  File object containing the metadata
     */
    public void addMetadataFile(File file)
    {
        String dataMetadata = "data/metadata/metadata.xml";
        FileBagFile fbf = new FileBagFile(dataMetadata, file);
        this.addMetadataBagFile(fbf);
    }

    /**
     * Set a licence file for the Bag
     *
     * @param file  File object containing the licence
     */
    public void addLicenceFile(File file)
    {
        this.addLicenceFile(file, null);
    }

    /**
     * Set a licence file for the Bag
     *
     * @param file  File object containing the licence
     * @param mimeType  mimetype of the licence object
     */
    public void addLicenceFile(File file, String mimeType)
    {
        // data licence directory
        String dataLicence = "data/licence/" + file.getName();

        // add the file
        theBag.putBagFile(new FileBagFile(dataLicence, file));

        // add the format to tagfiles/formats.txt
        if (mimeType == null)
        {
            mimeType = new MimetypesFileTypeMap().getContentType(file);
        }
        formats = formats + mimeType + "\t" + dataLicence + "\n";

        // generate the checksum
        String checksum = MessageDigestHelper.generateFixity(new FileBagFile(dataLicence, file).newInputStream(), Manifest.Algorithm.MD5);

        // add file to payload manifest
        manifest.put(dataLicence, checksum);
    }

    /**
     * Write the current state of the BagIt object out to the file the BagIt object is
     * constructed over
     */
    public void writeToFile()
    {
        // write all of our tagfiles
        StringBagFile formatBagFile = new StringBagFile("tagfiles/formats.txt", formats.getBytes());
        this.theBag.putBagFile(formatBagFile);
        String checksum = MessageDigestHelper.generateFixity(formatBagFile.newInputStream(), Manifest.Algorithm.MD5);
        tagmanifest.put("tagfiles/formats.txt", checksum);

        StringBagFile finalSeqBagFile = new StringBagFile("tagfiles/final.sequence.txt", finalSequence.getBytes());
        this.theBag.putBagFile(finalSeqBagFile);
        checksum = MessageDigestHelper.generateFixity(finalSeqBagFile.newInputStream(), Manifest.Algorithm.MD5);
        tagmanifest.put("tagfiles/final.sequence.txt", checksum);

        StringBagFile supportingAccessBagFile = new StringBagFile("tagfiles/supporting.access.txt", supportingAccess.getBytes());
        this.theBag.putBagFile(supportingAccessBagFile);
        checksum = MessageDigestHelper.generateFixity(supportingAccessBagFile.newInputStream(), Manifest.Algorithm.MD5);
        tagmanifest.put("tagfiles/supporting.access.txt", checksum);

        StringBagFile supportingSeqBagFile = new StringBagFile("tagfiles/supporting.sequence.txt", supportingSequence.getBytes());
        this.theBag.putBagFile(supportingSeqBagFile);
        checksum = MessageDigestHelper.generateFixity(supportingSeqBagFile.newInputStream(), Manifest.Algorithm.MD5);
        tagmanifest.put("tagfiles/supporting.sequence.txt", checksum);

        // write all the root directory stuff
        theBag.putBagFile(manifest);
        theBag.putBagFile(tagmanifest);

        BagItTxt bagItTxt = theBag.getBagPartFactory().createBagItTxt();
        theBag.putBagFile(bagItTxt);

        this.theBag.write(new ZipWriter(bagFactory), this.bagFile);
    }

    /**
     * Return the list of primary files sorted in sequence
     *
     * @return
     */
    public TreeMap<Integer, BaggedItem> getSequencedFinals() {

        TreeMap<Integer, BaggedItem> sequencedPrimaries = new TreeMap<Integer, BaggedItem>();

        // split our access rights string
        String[] theFinalSequence = finalSequence.split("\n");

        // for each line
        for(String line : theFinalSequence) {

            // split it  up
            String[] words = line.split("\\s");

            // check to make sure it's not a blank line
            if(words.length > 0)
            {

                // get the path
                String[] path = words[1].split("/");

                // the bagged item
                BaggedItem baggedItem = new BaggedItem();

                baggedItem.setInputStream(theBag.getBagFile(words[1]).newInputStream());
                baggedItem.setFilename(path[path.length - 1]);
                baggedItem.setFormat(formatMap.get(words[1]));
                baggedItem.setSequence(Integer.parseInt(words[0]));

                // create the node
                sequencedPrimaries.put(Integer.parseInt(words[0]), baggedItem);
            }
        }

        return sequencedPrimaries;
    }

    /**
     * Return the list of secondary files sorted in sequence
     *
     * @param accessRights  Access rights filter - provide "open" or "closed", to obtain only those with that access right
     * @return
     */
    public TreeMap<Integer, BaggedItem> getSequencedSecondaries(String accessRights) {

        TreeMap<Integer, BaggedItem> sequencedSecondaries = new TreeMap<Integer, BaggedItem>();

        // split our access rights string
        String[] theSupportingSequence = supportingSequence.split("\n");

        // for each line
        for(String line : theSupportingSequence) {

            // split it  up
            String[] words = line.split("\\s+");

            // check to make sure it's not a blank line and that the line is valid
            // skip if not
            if (words.length == 2)
            {

                // only if the access rights match
                if (accessRights.equals(accessMap.get(words[1]))) {

                    String[] path = words[1].split("/");

                    // the bagged item
                    BaggedItem baggedItem = new BaggedItem();

                    baggedItem.setInputStream(theBag.getBagFile(words[1]).newInputStream());
                    baggedItem.setFilename(path[path.length - 1]);
                    baggedItem.setFormat(formatMap.get(words[1]));
                    baggedItem.setSequence(Integer.parseInt(words[0]));

                    // create the node
                    sequencedSecondaries.put(Integer.parseInt(words[0]), baggedItem);
                }
            }
        }

        return sequencedSecondaries;
    }


    /**
     * Get the Metadata file from the Bag
     *
     * @return
     */
    public BaggedItem getMetadataFile()
    {
        BaggedItem metadata = new BaggedItem();
        metadata.setInputStream(theBag.getBagFile("data/metadata/metadata.xml").newInputStream());
        metadata.setFilename("metadata.xml");
        metadata.setFormat("text/xml");
        return metadata;
    }


    /**
     * Get the licence file from the Bag
     *
     * @return
     */
    public BaggedItem getLicenceFile() {

        BaggedItem licence = new BaggedItem();
        licence.setInputStream(theBag.getBagFile("data/licence/licence.txt").newInputStream());
        licence.setFilename("licence.txt");
        licence.setFormat("text/plain");

        return licence;
    }

    /**
     * Obtain the access privilege for the specified filename
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public String getSupportingAccess(String filename)
            throws IOException
    {
        return accessMap.get(filename);
    }

    /**
     * Verify the Bag against its manifest
     *
     * @return
     */
    public boolean verifyPayloadManifest()
    {

        return theBag.verifyPayloadManifests().isSuccess();
    }

    /**
     * Verify the Bag against its tag manifest
     *
     * @return
     */
    public boolean verifyTagManifest()
    {

        return theBag.verifyTagManifests().isSuccess();
    }

    /**
     * Get the file representing the whole bag
     *
     * @return
     * @throws IOException
     */
    public InputStream getFile()
            throws IOException
    {

        return FileUtils.openInputStream(this.bagFile);
    }

    /**
     * Get the filename for the whole Bag
     *
     * @return
     */
    public String getName() {

        return this.bagFile.getName();
    }

    /**
     * Get the MD5 for the whole Bag
     *
     * @return
     */
    public String getMD5() {

        return (MessageDigestHelper.generateFixity(this.bagFile, Manifest.Algorithm.MD5));
    }

    /**
     * Get the mimetype of the packaged Bag
     *
     * @return
     */
    public String getMimetype() {

        return "application/zip";
    }

    /**
     * Get the SWORDv2 packaging identifier for the Bag
     * @return
     */
    public String getPackaging() {

        return "http://duo.uio.no/terms/package/FSBagIt";
    }

    /**
     * Set the Manifest and Tag Files for the Bag
     *
     * @throws IOException
     */
    private void setManifestsAndTagfiles()
            throws IOException
    {
        manifest = theBag.getPayloadManifest(Manifest.Algorithm.MD5);
        tagmanifest = theBag.getTagManifest(Manifest.Algorithm.MD5);

        // get the formats
        StringWriter formatWriter = new StringWriter();
        IOUtils.copy(theBag.getBagFile("tagfiles/formats.txt").newInputStream(), formatWriter, "UTF-8");
        formats = formatWriter.toString();

        // get the final sequence
        StringWriter finalWriter = new StringWriter();
        IOUtils.copy(theBag.getBagFile("tagfiles/final.sequence.txt").newInputStream(), finalWriter, "UTF-8");
        finalSequence = finalWriter.toString();

        // get the supporting sequence
        StringWriter supportingWriter = new StringWriter();
        IOUtils.copy(theBag.getBagFile("tagfiles/supporting.sequence.txt").newInputStream(), supportingWriter, "UTF-8");
        supportingSequence = supportingWriter.toString();

        // get the supporting access
        StringWriter accessWriter = new StringWriter();
        IOUtils.copy(theBag.getBagFile("tagfiles/supporting.access.txt").newInputStream(), accessWriter, "UTF-8");
        supportingAccess = accessWriter.toString();

        setFormatMap();

        setAccessMap();

    }

    private void addMetadataBagFile(BagFile metadataBagFile)
    {
        // add the file
        theBag.putBagFile(metadataBagFile);

        // add the format to tagfiles/formats.txt
        formats = formats + "text/xml\t" + metadataBagFile.getFilepath() + "\n";

        // generate the checksum
        String checksum = MessageDigestHelper.generateFixity(metadataBagFile.newInputStream(), Manifest.Algorithm.MD5);

        // add file to payload manifest
        manifest.put(metadataBagFile.getFilepath(), checksum);
    }

    /*
        returns a hashmap of formats read from tagfiles/formats.txt
     */

    private void setFormatMap() {

        // hash map of formats
        formatMap = new HashMap<String, String>();

        // split our formats string
        String[] theFormats = formats.split("\n");

        // for each line
        for(String line : theFormats) {

            // split it  up
            String[] words = line.split("\\s");

            // check to make sure it's not a blank line
            if(words.length > 0)
            {
                // store it in the formatMap
                formatMap.put(words[1], words[0]);
            }
        }

    }

    /*
       returns a hashmap of access rights read from tagfiles/supporting.access.txt
    */

    private void setAccessMap() {

        // hash map of access rights
        accessMap = new HashMap<String, String>();

        // only proceed if there's any point
        if (supportingAccess == null || "".equals(supportingAccess))
        {
            return;
        }

        // split our access rights string
        String[] theRights = supportingAccess.split("\n");

        // for each line
        for (String line : theRights) {

            // split it  up
            String[] words = line.split("\\s");

            // check to make sure it's not a blank line and has the appropriate
            // number of parts.  Skip if invalid.
            if (words.length == 2)
            {
                // store it in the formatMap
                accessMap.put(words[1], words[0]);
            }
        }

    }

}
