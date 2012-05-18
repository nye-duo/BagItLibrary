package no.uio.duo.bagit;

/*
<?xml version="1.0" encoding="utf-8"?>
<fs:metadata xmlns:fs="http://studentweb.no/terms/"
             xmlns:dcterms="http://purl.org/dc/terms/">

    <!-- Representing the Student -->
    <fs:name>Thor Heyerdahl</fs:name>
    <fs:givenName>Thor</fs:givenName>
    <fs:familyName>Heyerdahl</fs:familyName>
    <fs:studentNumber>123456789</fs:studentNumber>
    <fs:uid>theyerdahl</fs:uid>
    <fs:foedselsnummer>987654321</fs:foedselsnummer>
    <fs:postalAddress>Colla Micheri, Italy</fs:postalAddress>
    <fs:email>t.heyerdahl@kontiki.com</fs:email>
    <fs:telephoneNumber>0047 123456</fs:telephoneNumber>

    <!-- Subject(s) -->
    <fs:subject>
        <fs:subjectCode>AST3220</fs:subjectCode>
        <fs:subjectTitle>Kosmologi I</fs:subjectTitle>
    </fs:subject>

    <!-- The awarding institution's unit code and name -->
    <fs:unitcode>123</fs:unitcode>
    <fs:unitName>Arkeologi, konservering og historie</fs:unitName>

    <!-- Basic bibliographic metadata -->
    <dcterms:title>101 days around some of the world</dcterms:title>

    <!-- 2nd title = Translated title - goes in dc.title.alternative in DSpace -->
    <dcterms:title xml:lang="nob">101 days in the Pacific</dcterms:title>
    <dcterms:language>nob</dcterms:language>
    <dcterms:abstract xml:lang="nob">Thor Heyerdahl og fem andre dro fra Peru til Raroia i en selvkonstruert balsafl�te ved navn
        Kon-Tiki.
    </dcterms:abstract>

    <!-- 2nd abstract = repeating dc.description.abstract field -->
    <dcterms:abstract>
        In the Kon-Tiki Expedition, Heyerdahl and five fellow adventurers went to Peru, where
        they constructed a pae-pae raft from balsa wood and other native materials, a raft that
        they called the Kon-Tiki.
    </dcterms:abstract>

    <dcterms:type>Master's thesis</dcterms:type>

    <!-- FIXME: disabled embargo for the time being, due to bug in DSpace -->

    <!-- Type of embargo: 'Open', 'Closed', 'Not electronically available', '5 years', 'Other'-->
    <!-- <fs:embargoType>5 years</fs:embargoType> -->

    <!-- Date on which embargo is lifted - 31-12-9999 means "never" -->
    <!-- <fs:embargoEndDate>01-01-2015</fs:embargoEndDate> -->

    <!-- Grade (if available) -->
    <fs:grade>pass</fs:grade>

</fs:metadata>
 */

import static org.junit.Assert.*;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MetadataTest
{
    @Test
    public void testMakeMetadata()
    {
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
        metadata.setEmbargo("5 years", new Date((new Date()).getTime() + 100000000000L)); // some random time in the future
        metadata.addField(Metadata.GRADE, "pass");

        System.out.println(metadata.toXML());
    }

    @Test
    public void testWriteRead()
    {
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
        Date end = new Date((new Date()).getTime() + 100000000000L);
        metadata.setEmbargo("5 years", end); // some random time in the future
        metadata.addField(Metadata.GRADE, "pass");

        System.out.println(metadata.toXML());

        assertEquals(metadata.getField(Metadata.NAME).get(0), "Thor Heyerdahl");
        assertEquals(metadata.getField(Metadata.FAMILY_NAME).get(0), "Heyerdahl");
        assertEquals(metadata.getField(Metadata.GIVEN_NAME).get(0), "Thor");
        assertEquals(metadata.getField(Metadata.STUDENT_NUMBER).get(0), "123456789");
        assertEquals(metadata.getField(Metadata.UID).get(0), "theyerdahl");
        assertEquals(metadata.getField(Metadata.FOEDSELSNUMMER).get(0), "987654321");
        assertEquals(metadata.getField(Metadata.POSTAL_ADDRESS).get(0), "Colla Micheri, Italy");
        assertEquals(metadata.getField(Metadata.EMAIL).get(0), "t.heyerdahl@kontiki.com");
        assertEquals(metadata.getField(Metadata.TELEPHONE_NUMBER).get(0), "0047 123456");
        assertEquals(metadata.getField(Metadata.UNITCODE).get(0), "123");
        assertEquals(metadata.getField(Metadata.UNIT_NAME).get(0), "Arkeologi, konservering og historie");
        assertEquals(metadata.getField(Metadata.LANGUAGE).get(0), "nob");
        assertEquals(metadata.getField(Metadata.TYPE).get(0), "Master's thesis");
        assertEquals(metadata.getField(Metadata.GRADE).get(0), "pass");
        assertEquals(metadata.getField(Metadata.EMBARGO_TYPE).get(0), "5 years");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(metadata.getField(Metadata.EMBARGO_END_DATE).get(0), sdf.format(end));

        List<String[]> subjects = metadata.getSubjects();
        assertEquals(subjects.size(), 1);
        assertEquals(subjects.get(0)[0], "AST3220");
        assertEquals(subjects.get(0)[1], "Kosmologi I");

        assertEquals(metadata.getField(Metadata.TITLE).size(), 2);
        assertEquals(metadata.getField(Metadata.ABSTRACT).size(), 2);

        assertEquals(metadata.getField(Metadata.TITLE, "nob").get(0), "101 days in the Pacific");
    }
}
