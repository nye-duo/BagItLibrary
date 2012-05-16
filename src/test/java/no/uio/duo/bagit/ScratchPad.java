package no.uio.duo.bagit;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.writer.impl.ZipWriter;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScratchPad
{
    @Test
    public void construct() throws Exception
    {
        String fileBase = System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        String out = System.getProperty("user.dir") + "/src/test/resources/testbags/fullbag.zip";

        BagIt bi = new BagIt(new File(out));

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();
    }
}
