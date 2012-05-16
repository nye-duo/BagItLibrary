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

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Metadata
{
    private static String FS_NAMESPACE = "http://studentweb.no/terms/";
    private static String DC_NAMESPACE = "http://purl.org/dc/terms/";
    private static String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    public static String NAME = "fs:name";
    public static String GIVEN_NAME = "fs:givenName";
    public static String FAMILY_NAME = "fs:familyName";
    public static String STUDENT_NUMBER = "fs:studentNumber";
    public static String UID = "fs:uid";
    public static String FOEDSELSNUMMER = "fs:foedselsnummer";
    public static String POSTAL_ADDRESS = "fs:postalAddress";
    public static String EMAIL = "fs:email";
    public static String TELEPHONE_NUMBER = "fs:telephoneNumber";
    public static String SUBJECT = "fs:subject";
    public static String SUBJECT_CODE = "fs:subjectCode";
    public static String SUBJECT_TITLE = "fs:subjectTitle";
    public static String UNITCODE = "fs:unitcode";
    public static String UNIT_NAME = "fs:unitName";
    public static String EMBARGO_TYPE = "fs:embargoType";
    public static String EMBARGO_END_DATE = "fs:embargoEndDate";
    public static String GRADE = "fs:grade";

    public static String TITLE = "dcterms:title";
    public static String LANGUAGE = "dcterms:language";
    public static String ABSTRACT = "dcterms:abstract";
    public static String TYPE = "dcterms:type";

    private Element metadata = null;

    public Metadata()
    {
        this.metadata = new Element("fs:metadata", FS_NAMESPACE);
        this.metadata.addNamespaceDeclaration("dcterms", DC_NAMESPACE);
    }

    public void addField(String fieldName, String value)
    {
        this.addField(fieldName, value, null);
    }

    public void addField(String fieldName, String value, String language)
    {
        String namespace = fieldName.startsWith("fs:") ? FS_NAMESPACE : DC_NAMESPACE;
        Element field = new Element(fieldName, namespace);
        if (language != null)
        {
            Attribute attribute = new Attribute("xml:lang", XML_NAMESPACE, language);
            field.addAttribute(attribute);
        }
        field.appendChild(value);
        this.metadata.appendChild(field);
    }

    public void addSubject(String code, String title)
    {
        Element subject = this.metadata.getFirstChildElement(SUBJECT, FS_NAMESPACE);
        if (subject == null)
        {
            subject = new Element(SUBJECT, FS_NAMESPACE);
            this.metadata.appendChild(subject);
        }

        Element subjectCode = new Element(SUBJECT_CODE, FS_NAMESPACE);
        subjectCode.appendChild(code);

        Element subjectTitle = new Element(SUBJECT_TITLE, FS_NAMESPACE);
        subjectTitle.appendChild(title);

        subject.appendChild(subjectCode);
        subject.appendChild(subjectTitle);
    }

    public void setEmbargo(String type, Date date)
    {
        this.addField(Metadata.EMBARGO_TYPE, type);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String end = sdf.format(date);
        this.addField(Metadata.EMBARGO_END_DATE, end);
    }

    public String toXML()
    {
        Document doc = new Document(this.metadata);
        return doc.toXML();
    }
}
