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

public class BagIt {

    // our BagFactory
    BagFactory bagFactory = new BagFactory();


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
    BagIt(String filePath) {

        Bag theBag = bagFactory.createBag(new File(filePath));

    }


    /*
        adds a file to our BagIt
     */
    public void addFile(String directory, InputStream inputStream) {

    }

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
