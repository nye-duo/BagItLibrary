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

import static org.junit.Assert.*;

import gov.loc.repository.bagit.*;
import gov.loc.repository.bagit.impl.ManifestWriterImpl;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BagItTest {

    /*
        create a bag from an existing directory in the correct format
     */

    @Test
    public void testBagConstructorExistingBag() throws Exception {

        BagIt testBagIt = new BagIt(System.getProperty("user.dir") + "/src/test/resources/testbags/testbag1");
        Bag bag = testBagIt.theBag;

        try {

            assertTrue(bag.verifyValid().isSuccess());
            assertEquals(4, bag.getPayload().size());
            assertEquals(1, bag.getTagManifests().size());
            BagItTxt bagIt = bag.getBagItTxt();
            assertEquals("UTF-8", bagIt.getCharacterEncoding());
            assertEquals(bag.getVersion().versionString, bagIt.getVersion());


        } finally {

            bag.close();
        }


    }

    /*
        create empty bag
     */

    @Test
    public void testBagConstructorEmptyBag() throws Exception {

        BagIt testBagIt = new BagIt();
        Bag bag = testBagIt.theBag;
        BagFactory bagFactory = testBagIt.bagFactory;
        Bag.BagConstants bagConstants =

        try {

            assertFalse(bag.verifyValid().isSuccess());  // we don't have a payload manifest or bagit.txt

            // now add some data files
            testBagIt.addPrimaryFile(new File(System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/test1.txt"));
            testBagIt.addPrimaryFile(new File(System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/test2.txt"));
            testBagIt.addPrimaryFile(new File(System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/bagitspec.pdf"));

            assertEquals(3, bag.getPayload().size());

            assertFalse(bag.verifyValid().isSuccess()); // we still don't have a valid bag... create our manifest

            bag.getBagConstants();

            // now add our manifest
            //BagFile bagItTxt = bag.getBagPartFactory().createBagItTxt();






            //bag.getBagPartFactory().createBagInfoTxt();
            //bag.getBagPartFactory().createManifest("manifest-md5.txt");

            //assertTrue(bag.verifyValid().isSuccess()); // are we now valid?

        }
        finally {

            bag.close();
        }

    }

    /*
        testing ability to add tag directory, i.e. secondary, licence and metadata
     */
    @Test
    public void testTagDirectoryAddition() throws Exception {

        BagIt testBagIt = new BagIt();
        Bag bag = testBagIt.theBag;

        try {



        }
        finally {

            bag.close();
        }
    }
}
