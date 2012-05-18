package no.uio.duo.bagit;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagItTxt;
import gov.loc.repository.bagit.writer.impl.ZipWriter;
import nu.xom.Builder;
import nu.xom.Document;
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

    @Test
    public void constructWithMetadata() throws Exception
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
        // String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        String out = System.getProperty("user.dir") + "/src/test/resources/testbags/fullbag.zip";

        Metadata metadata = new Metadata();

        metadata.addField(Metadata.NAME, "Thor Heyerdahl");
        metadata.addField(Metadata.FAMILY_NAME, "Heyerdahl");
        metadata.addField(Metadata.GIVEN_NAME, "Thor");
        metadata.addField(Metadata.STUDENT_NUMBER, "123456789");
        metadata.addField(Metadata.UID, "theyerdahl");
        metadata.addField(Metadata.FOEDSELSNUMMER, "987654321");
        metadata.addField(Metadata.POSTAL_ADDRESS, "Colla Micheri, Italy");
        metadata.addField(Metadata.EMAIL, "t.heyerdahl@kontiki.com");
        metadata.addField(Metadata.TELEPHONE_NUMBER, "0047 123456");
        metadata.addSubject("AST3220", "Kosmologi I");
        metadata.addField(Metadata.UNITCODE, "123");
        metadata.addField(Metadata.UNIT_NAME, "Arkeologi, konservering og historie");
        metadata.addField(Metadata.TITLE, "101 days around some of the world");
        metadata.addField(Metadata.TITLE, "101 days in the Pacific", "nob");
        metadata.addField(Metadata.LANGUAGE, "nob");
        metadata.addField(Metadata.ABSTRACT, "Thor Heyerdahl og fem andre dro fra Peru til Raroia i en selvkonstruert balsafl�te ved navn\n" +
                "        Kon-Tiki.", "nob");
        metadata.addField(Metadata.ABSTRACT, "In the Kon-Tiki Expedition, Heyerdahl and five fellow adventurers went to Peru, where\n" +
                "        they constructed a pae-pae raft from balsa wood and other native materials, a raft that\n" +
                "        they called the Kon-Tiki.");
        metadata.addField(Metadata.TYPE, "Master's thesis");
        metadata.addField(Metadata.EMBARGO_TYPE, "5 years");
        metadata.addField(Metadata.EMBARGO_END_DATE, "01-01-2015");
        metadata.addField(Metadata.GRADE, "pass");

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

        bi.addMetadata(metadata);
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();
    }

    @Test
    public void parsexml()
            throws Exception
    {
        String xmlorig = "<fs:metadata xmlns:fs=\"ftp://studentweb.no/terms/\"><fs:name>Thor Heyerdahl</fs:name><fs:familyName>Heyerdahl</fs:familyName><fs:givenName>Thor</fs:givenName><fs:studentNumber>123456789</fs:studentNumber><fs:uid>theyerdahl</fs:uid><fs:foedselsnummer>987654321</fs:foedselsnummer><fs:postalAddress>Colla Micheri, Italy</fs:postalAddress><fs:email>t.heyerdahl@kontiki.com</fs:email><fs:telephoneNumber>0047 123456</fs:telephoneNumber><fs:subject><fs:subjectCode>AST3220</fs:subjectCode><fs:subjectTitle>Kosmologi I</fs:subjectTitle></fs:subject><fs:unitcode>123</fs:unitcode><fs:unitName>Arkeologi, konservering og historie</fs:unitName><dcterms:title xmlns:dcterms=\"http://purl.org/dc/terms/\">101 days around some of the world</dcterms:title><dcterms:title xmlns:dcterms=\"http://purl.org/dc/terms/\" xml:lang=\"nob\">101 days in the Pacific</dcterms:title><dcterms:language xmlns:dcterms=\"http://purl.org/dc/terms/\">nob</dcterms:language><dcterms:abstract xmlns:dcterms=\"http://purl.org/dc/terms/\" xml:lang=\"nob\">Thor Heyerdahl og fem andre dro fra Peru til Raroia i en selvkonstruert balsafl?te ved navn\n" +
                "        Kon-Tiki.</dcterms:abstract><dcterms:abstract xmlns:dcterms=\"http://purl.org/dc/terms/\">In the Kon-Tiki Expedition, Heyerdahl and five fellow adventurers went to Peru, where\n" +
                "        they constructed a pae-pae raft from balsa wood and other native materials, a raft that\n" +
                "        they called the Kon-Tiki.</dcterms:abstract><dcterms:type xmlns:dcterms=\"http://purl.org/dc/terms/\">Master's thesis</dcterms:type><fs:embargoType>5 years</fs:embargoType><fs:embargoEndDate>01-01-2015</fs:embargoEndDate><fs:grade>pass</fs:grade></fs:metadata>";


        String xml = "<fs:metadata xmlns:fs=\"http://studentweb.no/terms/\"><fs:name>Thor Heyerdahl</fs:name><fs:familyName>Heyerdahl</fs:familyName><fs:givenName>Thor</fs:givenName><fs:studentNumber>123456789</fs:studentNumber><fs:uid>theyerdahl</fs:uid><fs:foedselsnummer>987654321</fs:foedselsnummer><fs:postalAddress>Colla Micheri, Italy</fs:postalAddress><fs:email>t.heyerdahl@kontiki.com</fs:email><fs:telephoneNumber>0047 123456</fs:telephoneNumber><fs:subject><fs:subjectCode>AST3220</fs:subjectCode><fs:subjectTitle>Kosmologi I</fs:subjectTitle></fs:subject><fs:unitcode>123</fs:unitcode><fs:unitName>Arkeologi, konservering og historie</fs:unitName><fs:embargoType>5 years</fs:embargoType><fs:embargoEndDate>01-01-2015</fs:embargoEndDate><fs:grade>pass</fs:grade></fs:metadata>";


        Builder parser = new Builder();
        Document doc = parser.build(xml);
        doc.getRootElement();
    }
}
