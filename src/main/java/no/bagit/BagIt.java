package no.bagit;

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

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

public class BagIt {

    // our BagFactory
    Bag theBag;
    BagFactory bagFactory = new BagFactory();
    Bag.BagConstants bagConstants = new Bag.BagConstants() {

        public String getPayloadManifestPrefix() {
            return null;
        }


        public String getTagManifestPrefix() {
            return null;
        }


        public String getPayloadManifestSuffix() {
            return null;
        }


        public String getTagManifestSuffix() {
            return null;
        }


        public String getBagEncoding() {
            return null;
        }


        public String getBagItTxt() {
            return null;
        }


        public String getDataDirectory() {
            return null;
        }


        public String getBagInfoTxt() {
            return null;
        }


        public String getFetchTxt() {
            return null;
        }


        public BagFactory.Version getVersion() {
            return null;
        }
    };


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
    public BagIt(String filePath) {

        theBag = bagFactory.createBag(new File(filePath));

    }

    public BagIt(File file) {

        theBag = bagFactory.createBag(file);

    }

    /*
        creates an empty BagIt
     */
    public BagIt() {
        theBag = bagFactory.createBag();

        // create the data directory

        // create the supporting directory

        // create the licence directory

        // create the metadata directory

        // create the tagfiles directory

        // create tagfiles/supporting.access.txt
        // create tagfiles/formats.txt
        // create tagfiles/final.sequence.txt
        // create tagfiles/supporting.sequence.txt
    }

    /*
        adds a payload file to our BagIt
     */
    public void addPrimaryFile(File file) {

        // add the file into the final directory (and manifest-md5.txt)
        theBag.addFileToPayload(file);

        // add the format to tagfiles/formats.txt

        // add the file to the final.sequence.txt


    }

    public void addSecondaryFile(File file, String access) {

        // add the file into the supporting directory
        theBag.addFileToPayload(file);

        // add the file tagfiles/supporting.access.txt as access (open|closed)

        // add the format to tagfiles/formats.txt

        // add the file to the supporting.sequence.txt

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
    public void addMetadataFile(File file) {

        // add the format to tagfiles/formats.txt

    }

    /*
        add a licence file in the licence directory
     */
    public void addLicenceFile(File file) {

        // add the format to tagfiles/formats.txt

    }

    /*
        generates the payload manifest
     */

    /*
        generates the tag manifest
     */



    /*
       get the licence for the item
     */


    /*
        returns the list of primary files in sequence
        data/final/[final version files] ordered by tagfiles/final.sequence.txt

        R008 DSpace Item PRIMARY
     */
    public InputStream getPrimary() {

        InputStream primary = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        return primary;
    }

    public TreeMap<Integer, BaggedItem> getSequencedPrimaries()
    {
        return null;
    }

    public TreeMap<Integer, BaggedItem> getSequencedSecondaries(String accessRights)
    {
        return null;
    }

    public BaggedItem getMetadata()
    {
        return null;
    }

    public BaggedItem getLicence()
    {
        return null;
    }


    /*
       returns the list of secondary files in sequence
       data/supporting/[supporting files] ordered by tagfiles/supporting.sequence.txt

       R009 DSpace Item SECONDARY and R010 SECONDARY_RESTRICTED
    */
    public InputStream getSecondary() {

        InputStream secondary = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        return secondary;
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



    /*
        Looks for access set for filename in
        tagfiles/supporting.access.txt

        will be open or closed
        open -> DSpace Item SECONDARY
        closed -> DSpace Item SECONDARY_RESTRICTED
    */
    public String getSupportingAccess(String filename) {

        return "open";
    }

    /*
        returns the metadata file
        data/metadata/metadata.xml

        DSpace Item METADATA
     */


} // end public class BagIt
