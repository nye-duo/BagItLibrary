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
import gov.loc.repository.bagit.utilities.MessageDigestHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class BagIt {

    // our BagFactory
    Bag theBag;
    BagFactory bagFactory = new BagFactory();
    Manifest manifest;
    Manifest tagmanifest;

    File formats;
    File finalSequence;
    File supportingSequence;
    File supportingAccess;

    /*
        creates a BagIt from an existing directory organised in the correct
        format

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
    */
    public BagIt(String filePath) throws IOException {

        theBag = bagFactory.createBag(new File(filePath));

        setManifestsAndTagfiles();

    }

    public BagIt(File file) throws IOException {

        theBag = bagFactory.createBag(file);

        setManifestsAndTagfiles();
    }

    /*
        creates an empty BagIt according to our BagIt structure
     */
    public BagIt() {

        // create the bag
        theBag = bagFactory.createBag(BagFactory.Version.V0_97);

        // create our manifests
        manifest = theBag.getBagPartFactory().createManifest(ManifestHelper.getPayloadManifestFilename(Manifest.Algorithm.MD5, theBag.getBagConstants()));
        tagmanifest = theBag.getBagPartFactory().createManifest(ManifestHelper.getTagManifestFilename(Manifest.Algorithm.MD5, theBag.getBagConstants()));

        // create the tagfiles
        try {

            //create tagfiles directory

            // create tagfiles/formats.txt
            formats = new File("formats.txt");
            FileUtils.touch(formats);
            theBag.addFileAsTag(formats);

            // create tagfiles/final.sequence.txt
            finalSequence = new File("final.sequence.txt");
            FileUtils.touch(finalSequence);
            theBag.addFileAsTag(finalSequence);

            // create tagfiles/supporting.sequence.txt
            supportingSequence = new File("supporting.sequence.txt");
            FileUtils.touch(supportingSequence);
            theBag.addFileAsTag(supportingSequence);

            // create tagfiles/supporting.access.txt
            supportingAccess = new File("supporting.access.txt");
            FileUtils.touch(supportingAccess);
            theBag.addFileAsTag(supportingAccess);


        }
        catch (IOException e) {

            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    public void setManifestsAndTagfiles() throws IOException {

        manifest = theBag.getPayloadManifest(Manifest.Algorithm.MD5);
        tagmanifest = theBag.getTagManifest(Manifest.Algorithm.MD5);

        formats = new File("formats.txt");
        FileUtils.copyInputStreamToFile(theBag.getBagFile("tagfiles/formats.txt").newInputStream(), formats);

        finalSequence = new File("final.sequence.txt");
        FileUtils.copyInputStreamToFile(theBag.getBagFile("tagfiles/final.sequence.txt").newInputStream(), finalSequence);

        supportingSequence = new File("supporting.sequence.txt");
        FileUtils.copyInputStreamToFile(theBag.getBagFile("tagfiles/supporting.sequence.txt").newInputStream(), supportingSequence);

        supportingAccess = new File("supporting.access.txt");
        FileUtils.copyInputStreamToFile(theBag.getBagFile("tagfiles/supporting.access.txt").newInputStream(), supportingAccess);
    }

    /*
        adds a final file to our BagIt
     */

    public void addFinalFile(File file) throws IOException {

        // new file (according to our nomenclature
        File finalFile = new File("final/" + file.getName());

        // copy the file to where we need it
        FileUtils.copyFile(file, finalFile);

        // add the file into the final directory (and manifest-md5.txt)
        theBag.addFileToPayload(finalFile.getParentFile());
        // theBag.putBagFile(new FileBagFile("data/final", finalFile));

        // data final directory
        String dataFinal = "data/final/";

        // add the format to tagfiles/formats.txt
        FileUtils.writeStringToFile(formats, new MimetypesFileTypeMap().getContentType(file) + "\t" + dataFinal + finalFile.getName() + "\n", true);

        // add the file to the final.sequence.txt
        FileUtils.writeStringToFile(finalSequence, dataFinal + finalFile.getName() + "\n", true);

        // generate the checksum
        String checksum = MessageDigestHelper.generateFixity(new FileBagFile(dataFinal + finalFile.getName(), finalFile).newInputStream(), Manifest.Algorithm.MD5);

        // add file to payload manifest
        manifest.put(dataFinal + finalFile.getName(), checksum);
    }

    /*
        adds a supporting file to our BagIt
     */

    public void addSupportingFile(File file, String access) throws IOException {

        // new file (according to our nomenclature
        File supportingFile = new File("supporting/" + file.getName());

        // copy the file to where we need it
        FileUtils.copyFile(file, supportingFile);

        // add the file into the supporting directory
        theBag.addFileToPayload(supportingFile.getParentFile());

        // data supporting directory
        String dataSupporting = "data/supporting/";

        // add the format to tagfiles/formats.txt
        FileUtils.writeStringToFile(formats, new MimetypesFileTypeMap().getContentType(file) + "\t" + dataSupporting + supportingFile.getName() + "\n", true);

        // add the file to the supporting.sequence.txt
        FileUtils.writeStringToFile(supportingSequence, dataSupporting + file.getName() + "\n", true);

        // add the file tagfiles/supporting.access.txt as access (open|closed)
        FileUtils.writeStringToFile(supportingAccess, access + "\t" + dataSupporting + supportingFile.getName() + "\n", true);

    }


    /*
        adds a tag file to our BagIt
     */
    public void addTagFile(File file) {

        theBag.addFileAsTag(file);
    }


    /*
        add a metadata file in the metadata directory
     */
    public void addMetadataFile(File file) throws IOException {

        // new file (according to our nomenclature
        File metadataFile = new File("metadata/" + file.getName());

        // copy the file to where we need it
        FileUtils.copyFile(file, metadataFile);

        // add the file into the metadata directory
        theBag.addFileToPayload(metadataFile.getParentFile());

        // data metadata directory
        String dataMetadata = "data/metadata/";

        // add the format to tagfiles/formats.txt
        FileUtils.writeStringToFile(formats, new MimetypesFileTypeMap().getContentType(file) + "\t" + dataMetadata + metadataFile.getName() + "\n", true);

    }

    /*
        add a licence file in the licence directory
     */
    public void addLicenceFile(File file) throws IOException {

        // new file (according to our nomenclature
        File licenceFile = new File("licence/" + file.getName());

        // copy the file to where we need it
        FileUtils.copyFile(file, licenceFile);

        // add the file into the licence directory
        theBag.addFileToPayload(licenceFile.getParentFile());

        // data metadata directory
        String dataLicence = "data/licence/";

        // add the format to tagfiles/formats.txt
        FileUtils.writeStringToFile(formats, new MimetypesFileTypeMap().getContentType(file) + "\t" + dataLicence + licenceFile.getName() + "\n", true);

    }

    /*
        generates the manifests
     */

    public void generateManifests() {

        theBag.putBagFile(theBag.getBagPartFactory().createBagInfoTxt());
        theBag.putBagFile(theBag.getBagPartFactory().createBagItTxt());
        theBag.putBagFile(manifest);
        theBag.putBagFile(tagmanifest);
        theBag.makeComplete();

    }


    /*
        returns a hashmap of formats read from tagfiles/formats.txt
     */

    public HashMap<String, String> getFormatMap() throws IOException {

        // hash map of formats
        HashMap<String, String> formatMap = new HashMap<String, String>();

        // bag file of formats
        BagFile bagFormats = theBag.getBagFile("tagfiles/formats.txt");

        // create the file for the formats
        File bagFormatsFile = new File("formats.txt");

        // copy the contents from the bag file to the file
        FileUtils.copyInputStreamToFile(bagFormats.newInputStream(), bagFormatsFile);

        // create the line iterator
        LineIterator iterator = FileUtils.lineIterator(bagFormatsFile, "UTF-8");

        try {

            // for each line in the file
            while (iterator.hasNext()) {

                // get the line
                String line = iterator.nextLine();

                // split it  up
                String[] words = line.split("\\s+");

                // store it in the formatMap
                formatMap.put(words[1], words[0]);
            }
        }

        finally {

            // close the line iterator
            LineIterator.closeQuietly(iterator);
        }

        // return the hash map of formats
        return formatMap;

    }

    /*
       returns a hashmap of access rights read from tagfiles/supporting.access.txt
    */

    public HashMap<String, String> getAccessMap() throws IOException {

        // hash map of access rights
        HashMap<String, String> accessMap = new HashMap<String, String>();

        // bag file of access rights
        BagFile bagAccessRights = theBag.getBagFile("tagfiles/supporting.access.txt");

        // create the file for the formats
        File bagAccessFile = new File("supporting.access.txt");

        // copy the contents from the bag file to the file
        FileUtils.copyInputStreamToFile(bagAccessRights.newInputStream(), bagAccessFile);

        // create the line iterator
        LineIterator iterator = FileUtils.lineIterator(bagAccessFile, "UTF-8");

        try {

            // for each line in the file
            while (iterator.hasNext()) {

                // get the line
                String line = iterator.nextLine();

                // split it  up
                String[] words = line.split("\\s+");

                // store it in the formatMap
                accessMap.put(words[1], words[0]);
            }
        }

        finally {

            // close the line iterator
            LineIterator.closeQuietly(iterator);
        }

        // return the hash map of formats
        return accessMap;

    }


    /*
        returns the list of primary files in sequence
        data/final/[final version files] ordered by tagfiles/final.sequence.txt

        R008 DSpace Item PRIMARY
     */

    public TreeMap<Integer, BaggedItem> getSequencedPrimaries() throws IOException {

        // get the formats hash map
        HashMap<String, String> formatMap = getFormatMap();

        // look through tagfiles/final.sequence.txt
        BagFile bagFinalSequence = theBag.getBagFile("tagfiles/final.sequence.txt");

        // create the file
        File bagFinalFile = new File("final.sequence.txt");

        // copy the file
        FileUtils.copyInputStreamToFile(bagFinalSequence.newInputStream(), bagFinalFile);

        // get each BagFile in sequence and put it in a BaggedItem

        // read through the file, line by line
        LineIterator it = FileUtils.lineIterator(bagFinalFile, "UTF-8");

        TreeMap<Integer, BaggedItem> sequencedPrimaries = new TreeMap<Integer, BaggedItem>();

        try {
            while (it.hasNext()) {

                // get the line
                String line = it.nextLine();

                // split it  up
                String[] words = line.split("\\s+");

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
        } finally {
            LineIterator.closeQuietly(it);
        }
        // add it to our tree

        return sequencedPrimaries;
    }


    /*
       returns the list of secondary files in sequence
       data/supporting/[supporting files] ordered by tagfiles/supporting.sequence.txt

       R009 DSpace Item SECONDARY and R010 SECONDARY_RESTRICTED
    */


    public TreeMap<Integer, BaggedItem> getSequencedSecondaries(String accessRights) throws IOException {

        // get the formats hash map
        HashMap<String, String> formatMap = getFormatMap();

        // get the access hash map
        HashMap<String, String> accessMap = getAccessMap();

        // look through tagfiles/supporting.sequence.txt
        BagFile bagSupportingSequence = theBag.getBagFile("tagfiles/supporting.sequence.txt");

        // create the file
        File bagSupportingFile = new File("supporting.sequence.txt");

        // copy the file
        FileUtils.copyInputStreamToFile(bagSupportingSequence.newInputStream(), bagSupportingFile);

        // get each BagFile in sequence and put it in a BaggedItem

        // read through the file, line by line
        LineIterator it = FileUtils.lineIterator(bagSupportingFile, "UTF-8");

        TreeMap<Integer, BaggedItem> sequencedSecondaries = new TreeMap<Integer, BaggedItem>();

        try {
            while (it.hasNext()) {

                // get the line
                String line = it.nextLine();

                // split it  up
                String[] words = line.split("\\s+");

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
        finally {

            LineIterator.closeQuietly(it);
        }
        // add it to our tree

        return sequencedSecondaries;
    }


    /*
      returns the metadata
      data/metadata/metadata.xml
    */

    public BaggedItem getMetadata()
    {
        BaggedItem metadata = new BaggedItem();
        metadata.setInputStream(theBag.getBagFile("data/metadata/metadata.xml").newInputStream());
        metadata.setFilename("metadata.xml");
        metadata.setFormat("text/xml");

        return metadata;
    }


    /*
      returns the licence for the item
      data/licence/licence.txt
    */

    public BaggedItem getLicence()
    {
        BaggedItem licence = new BaggedItem();
        licence.setInputStream(theBag.getBagFile("data/licence/licence.txt").newInputStream());
        licence.setFilename("licence.txt");
        licence.setFormat("text/plain");

        return licence;
    }

    /*
        Looks for access set for filename in
        tagfiles/supporting.access.txt

        will be open or closed
        open -> DSpace Item SECONDARY
        closed -> DSpace Item SECONDARY_RESTRICTED
    */
    public String getSupportingAccess(String filename) throws IOException {

        // get the access hash map
        HashMap<String, String> accessMap = getAccessMap();

        return accessMap.get(filename);

    }

    /*
       verifies the bag against it's manifest
    */
    public boolean verifyPayloadManifest() {

        return theBag.verifyPayloadManifests().isSuccess();
    }

    /*
        verifies the bag against it's tag manifest
     */
    public boolean verifyTagManifest() {

        return theBag.verifyTagManifests().isSuccess();
    }



} // end public class BagIt
