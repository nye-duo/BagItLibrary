package no.uio.duo.bagit;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class representing the metadata for StudentWeb/FS.  It deals with creating and parsing
 * metadata files of the appropriate format.  The serialised version of the metadata looks
 * like this:
 *
 * <pre>
&lt;?xml version="1.0" encoding="utf-8"?&gt;
&lt;fs:metadata xmlns:fs="http://studentweb.no/terms/"
             xmlns:dcterms="http://purl.org/dc/terms/"&gt;

    &lt;!-- Representing the Student --&gt;
    &lt;fs:name&gt;Thor Heyerdahl&lt;/fs:name&gt;
    &lt;fs:givenName&gt;Thor&lt;/fs:givenName&gt;
    &lt;fs:familyName&gt;Heyerdahl&lt;/fs:familyName&gt;
    &lt;fs:studentNumber&gt;123456789&lt;/fs:studentNumber&gt;
    &lt;fs:uid&gt;theyerdahl&lt;/fs:uid&gt;
    &lt;fs:foedselsnummer&gt;987654321&lt;/fs:foedselsnummer&gt;
    &lt;fs:postalAddress&gt;Colla Micheri, Italy&lt;/fs:postalAddress&gt;
    &lt;fs:email&gt;t.heyerdahl@kontiki.com&lt;/fs:email&gt;
    &lt;fs:telephoneNumber&gt;0047 123456&lt;/fs:telephoneNumber&gt;

    &lt;!-- Subject(s) --&gt;
    &lt;fs:subject&gt;
        &lt;fs:subjectCode&gt;AST3220&lt;/fs:subjectCode&gt;
        &lt;fs:subjectTitle&gt;Kosmologi I&lt;/fs:subjectTitle&gt;
    &lt;/fs:subject&gt;

    &lt;!-- The awarding institution's unit code and name --&gt;
    &lt;fs:unitcode&gt;123&lt;/fs:unitcode&gt;
    &lt;fs:unitName&gt;Arkeologi, konservering og historie&lt;/fs:unitName&gt;

    &lt;!-- Basic bibliographic metadata --&gt;
    &lt;dcterms:title&gt;101 days around some of the world&lt;/dcterms:title&gt;

    &lt;!-- 2nd title = Translated title - goes in dc.title.alternative in DSpace --&gt;
    &lt;dcterms:title xml:lang="nob"&gt;101 days in the Pacific&lt;/dcterms:title&gt;
    &lt;dcterms:language&gt;nob&lt;/dcterms:language&gt;
    &lt;dcterms:abstract xml:lang="nob"&gt;Thor Heyerdahl og fem andre dro fra Peru til Raroia i en selvkonstruert balsafl�te ved navn
        Kon-Tiki.
    &lt;/dcterms:abstract&gt;

    &lt;!-- 2nd abstract = repeating dc.description.abstract field --&gt;
    &lt;dcterms:abstract&gt;
        In the Kon-Tiki Expedition, Heyerdahl and five fellow adventurers went to Peru, where
        they constructed a pae-pae raft from balsa wood and other native materials, a raft that
        they called the Kon-Tiki.
    &lt;/dcterms:abstract&gt;

    &lt;dcterms:type&gt;Master's thesis&lt;/dcterms:type&gt;

    &lt;!-- Type of embargo: 'Open', 'Closed', 'Not electronically available', '5 years', 'Other'--&gt;
    &lt;fs:embargoType&gt;5 years&lt;/fs:embargoType&gt;

    &lt;!-- Date on which embargo is lifted - 31-12-9999 means "never" --&gt;
    &lt;fs:embargoEndDate&gt;01-01-2015&lt;/fs:embargoEndDate&gt;

    &lt;!-- Grade (if available) --&gt;
    &lt;fs:grade&gt;pass&lt;/fs:grade&gt;

&lt;/fs:metadata&gt;
 * </pre>
 */

public class Metadata
{
    /** Namespace of the FS Metadata */
    private static String FS_NAMESPACE = "http://studentweb.no/terms/";

    /** Namespace of the DC Terms metadata elements */
    private static String DC_NAMESPACE = "http://purl.org/dc/terms/";

    /** The XML namespace */
    private static String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    /** FS metadata student name element (with namespace prefix) */
    public static String NAME = "fs:name";

    /** FS metadata student's given name element (with namespace prefix) */
    public static String GIVEN_NAME = "fs:givenName";

    /** FS metadata student's family name element (with namespace prefix) */
    public static String FAMILY_NAME = "fs:familyName";

    /** FS metadata student number element (with namespace prefix) */
    public static String STUDENT_NUMBER = "fs:studentNumber";

    /** FS metadata student UID element (with namespace prefix) */
    public static String UID = "fs:uid";

    /** FS metadata student's national insurance number element (with namespace prefix) */
    public static String FOEDSELSNUMMER = "fs:foedselsnummer";

    /** FS metadata student's postal address element (with namespace prefix) */
    public static String POSTAL_ADDRESS = "fs:postalAddress";

    /** FS metadata student's email element (with namespace prefix) */
    public static String EMAIL = "fs:email";

    /** FS metadata student's telephone number element (with namespace prefix) */
    public static String TELEPHONE_NUMBER = "fs:telephoneNumber";

    /** FS metadata item's subject element (with namespace prefix) */
    public static String SUBJECT = "fs:subject";

    /** FS metadata item's subject code element (with namespace prefix) */
    public static String SUBJECT_CODE = "fs:subjectCode";

    /** FS metadata item's subject title element (with namespace prefix) */
    public static String SUBJECT_TITLE = "fs:subjectTitle";

    /** FS metadata student's department's unit code element (with namespace prefix) */
    public static String UNITCODE = "fs:unitcode";

    /** FS metadata student's department's unit name element (with namespace prefix) */
    public static String UNIT_NAME = "fs:unitName";

    /** FS metadata item's embargo type element (with namespace prefix) */
    public static String EMBARGO_TYPE = "fs:embargoType";

    /** FS metadata item's embargo end date element (with namespace prefix) */
    public static String EMBARGO_END_DATE = "fs:embargoEndDate";

    /** FS metadata grade element (with namespace prefix) */
    public static String GRADE = "fs:grade";

    /** FS metadata item title element (with namespace prefix) */
    public static String TITLE = "dcterms:title";

    /** FS metadata item language element (with namespace prefix) */
    public static String LANGUAGE = "dcterms:language";

    /** FS metadata item abstract element (with namespace prefix) */
    public static String ABSTRACT = "dcterms:abstract";

    /** FS metadata item type element (with namespace prefix) */
    public static String TYPE = "dcterms:type";

    /** wildcard parameter to match any language term */
    public static String ANY_LANGUAGE = "*";

    private Element metadata = null;

    /**
     * Construct a new Metadata object from scratch
     */
    public Metadata()
    {
        this.metadata = new Element("fs:metadata", FS_NAMESPACE);
        this.metadata.addNamespaceDeclaration("dcterms", DC_NAMESPACE);
    }

    /**
     * Construct a new Metadata object around the Element instance which
     * should be in the correct form
     *
     * @param metadata Element object
     */
    public Metadata(Element metadata)
    {
        this.metadata = metadata;
    }

    /**
     * Add the given value to the given field.  The field should contain the relevant
     * metadata prefix (e.g. dcterms:title).  Recommended to use the static constants
     * attached to this class
     *
     * @param fieldName Field name to add value to
     * @param value value to add to the field
     */
    public void addField(String fieldName, String value)
    {
        this.addField(fieldName, value, null);
    }

    /**
     * Add the given value to the given field with the given language code.  The field should contain the relevant
     * metadata prefix (e.g. dcterms:title).  Recommended to use the static constants for the field name which are
     * attached to this class
     *
     * @param fieldName Field name to add value to
     * @param value value to add to the field
     * @param language  language of the field contents
     */
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

    /**
     * Add the given related subject code and title to the item
     *
     * @param code  subject code
     * @param title subject title
     */
    public void addSubject(String code, String title)
    {
        Element subject = new Element(SUBJECT, FS_NAMESPACE);

        Element subjectCode = new Element(SUBJECT_CODE, FS_NAMESPACE);
        subjectCode.appendChild(code);

        Element subjectTitle = new Element(SUBJECT_TITLE, FS_NAMESPACE);
        subjectTitle.appendChild(title);

        subject.appendChild(subjectCode);
        subject.appendChild(subjectTitle);

        this.metadata.appendChild(subject);
    }

    /**
     * Set the embargo information for the item
     *
     * @param type  Embargo type
     * @param date  Embargo date
     */
    public void setEmbargo(String type, Date date)
    {
        this.addField(Metadata.EMBARGO_TYPE, type);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String end = sdf.format(date);
        this.addField(Metadata.EMBARGO_END_DATE, end);
    }

    /**
     * Get the list of values currently in the given field.  The field should contain the relevant
     * metadata prefix (e.g. dcterms:title).
     *
     * @param fieldName The field from which to get the values
     * @return
     */
    public List<String> getField(String fieldName)
    {
        return this.getField(fieldName, ANY_LANGUAGE);
    }

    /**
     * Get the list of values currently in the given field with the specified language.  The field should contain the relevant
     * metadata prefix (e.g. dcterms:title).  To search for any language use {@link #ANY_LANGUAGE}
     *
     * @param fieldName The field from which to get the values
     * @param language  The language code to filter by
     * @return
     */
    public List<String> getField(String fieldName, String language)
    {
        String[] parts = this.interpretField(fieldName);
        List<String> values = new ArrayList<String>();
        Elements elements = this.metadata.getChildElements(parts[0], parts[1]);
        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            Attribute lang = element.getAttribute("lang", XML_NAMESPACE);

            if (    (language == null || ANY_LANGUAGE.equals(language)) || // if the language argument is null or set to * OR
                    (language != null && !ANY_LANGUAGE.equals(language) && lang != null && language.equals(lang.getValue())) // if the language value is set and is equal to the xml:lang attribute (if it is not null)
               )
            {
                values.add(element.getValue());
            }
        }
        return values;
    }

    /**
     * Get a list of the subject code/title pairs.
     *
     * @return  A list of string arrays, where each array is two elements long, the first element containing
     *          the subject code, and the second element containing the subject title.
     */
    public List<String[]> getSubjects()
    {
        List<String[]> subjects = new ArrayList<String[]>();
        String[] parts = this.interpretField(SUBJECT);
        Elements elements = this.metadata.getChildElements(parts[0], parts[1]);
        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            String[] codeParts = this.interpretField(SUBJECT_CODE);
            Element codeElement = element.getFirstChildElement(codeParts[0], codeParts[1]);
            String[] titleParts = this.interpretField(SUBJECT_TITLE);
            Element titleElement = element.getFirstChildElement(titleParts[0], titleParts[1]);
            String[] pair = { codeElement.getValue(), titleElement.getValue() };
            subjects.add(pair);
        }
        return subjects;
    }

    /**
     * Generate an XML string representing the metadata
     *
     * @return
     */
    public String toXML()
    {
        Document doc = new Document(this.metadata);
        return doc.toXML();
    }

    /**
     * Get the root XML metadata element
     *
     * @return
     */
    public Element getElement()
    {
        return this.metadata;
    }

    private String[] interpretField(String fieldName)
    {
        String[] bits = fieldName.split(":");
        if (bits.length == 1)
        {
            String[] ret = { bits[0], null };
            return ret;
        }
        else if (bits.length == 2)
        {
            String namespace = bits[0].startsWith("fs") ? FS_NAMESPACE : DC_NAMESPACE;
            String[] ret = {bits[1], namespace};
            return ret;
        }
        else
        {
            return null;
        }
    }
}
